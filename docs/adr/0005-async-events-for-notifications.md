# 5. Asynchronous events for notifications

Date: 2026-07-11

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

When an invoice is issued, a notification has to be produced. The invoice-service
already talks to the customer-service **synchronously** via Feign (it needs the
customer to exist *before* it can create an invoice — the call is on the critical
path). The notification is different: it is a **side effect** of issuing an invoice,
not a precondition for it.

Doing it synchronously (invoice-service calls a notification-service REST endpoint
inside `issue()`) would mean:

- Issuing an invoice fails or slows down if the notification-service is down or slow.
- The two services are temporally coupled — both must be up at the same time.
- The invoice-service has to know the notification-service's API and address.

## Decision

Cross-service **notifications are delivered asynchronously via events over RabbitMQ**.

- On `issue()`, the invoice-service publishes an `InvoiceIssued` event to a topic
  exchange (`billing.events`, routing key `invoice.issued`) and returns immediately.
- The notification-service binds a durable queue to that exchange and consumes the
  events on its own time, in its own transaction.
- The two services share no code: each defines its own copy of the event contract,
  and unknown JSON fields are ignored (tolerant reader).

Synchronous Feign is kept only where a call is genuinely on the critical path
(invoice → customer validation).

## Consequences

**Positive**

- Temporal decoupling: issuing an invoice succeeds even if the notification-service
  is down; the durable queue holds events until it comes back.
- The invoice-service does not know or care who consumes the event — more consumers
  can be added later without touching it.
- Load spikes are absorbed by the queue instead of hitting a synchronous endpoint.

**Negative / trade-offs**

- Consistency is **eventual**: the notification appears shortly *after* the invoice
  is issued, not within the same transaction.
- New infrastructure to run and reason about (a broker), plus delivery semantics to
  handle — RabbitMQ is at-least-once, so consumers must be idempotent (see
  [ADR-0006](0006-idempotent-consumer.md)).
- Publishing happens after the DB commit inside `issue()`; a crash in the small
  window between commit and publish could drop an event. A transactional outbox
  would close this gap and is deliberately deferred.
