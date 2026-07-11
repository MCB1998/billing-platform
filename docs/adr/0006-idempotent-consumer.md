# 6. Idempotent consumer

Date: 2026-07-11

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

RabbitMQ (like most brokers) guarantees **at-least-once** delivery: a message can be
delivered more than once — e.g. when a consumer processes a message but its
acknowledgement is lost, the broker redelivers it. If the notification-service
naively inserted a row per delivery, a single `InvoiceIssued` event could produce
duplicate notifications.

## Decision

The notification-service is an **idempotent consumer**, keyed on the event's `eventId`
(a UUID set by the publisher, carried in the event body):

- Each `InvoiceIssued` event carries a unique `eventId`.
- Before recording a notification, the service checks `existsByEventId(eventId)` and
  **skips** the event if it was already processed.
- As the ultimate guard against a duplicate slipping past the check under concurrency,
  the `notification_log.event_id` column has a **UNIQUE constraint** — a duplicate
  insert fails at the database, not just in application logic.

## Consequences

**Positive**

- Duplicate deliveries are harmless: an event is recorded at most once.
- Correctness does not depend on the check-then-insert being atomic — the database
  constraint backs it up.
- The design tolerates redelivery, which is exactly what an at-least-once broker
  requires (see [ADR-0005](0005-async-events-for-notifications.md)).

**Negative / trade-offs**

- The publisher must generate and carry a stable `eventId`; idempotency is a shared
  contract between publisher and consumer, not a consumer-only concern.
- The check is an extra read per event; acceptable here, and the unique index that
  enforces correctness also serves the lookup.
- Idempotency covers duplicate *delivery*, not duplicate *business events*: if the
  invoice-service published two events with different ids for the same issue, both
  would be recorded. Preventing that is the publisher's responsibility.
