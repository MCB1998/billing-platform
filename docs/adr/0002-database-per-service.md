# 2. Database-per-service

Date: 2026-07-07

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

Each microservice needs to persist data. The two common options are a single
shared database used by all services, or a separate database owned by each
service. A shared database couples services through the schema and undermines
their independence.

## Decision

Each service owns its **own PostgreSQL database**. No service reads or writes
another service's tables directly; cross-service data is exchanged only through
public APIs (synchronous) or events (asynchronous).

## Consequences

**Positive**

- Loose coupling: a service can evolve its schema and deploy independently.
- Clear ownership and encapsulation of data.
- Failure isolation at the data layer.

**Negative / trade-offs**

- No cross-service SQL joins; data is combined at the application level.
- Consistency across services becomes eventual (via events), not transactional.
- More infrastructure: one database instance per service (in local dev, one
  container per service via docker-compose).
