# 4. Flyway for PostgreSQL, Hibernate schema generation for H2 dev/tests

Date: 2026-07-07

## Status

Accepted (who wants to oppose you if you're alone hehe)


## Context

The schema needs to be managed for two setups: a fast, zero-setup H2 in-memory
database for local development and most tests, and real PostgreSQL for
production-like operation. Hibernate's `ddl-auto` can generate the schema from
the entities, but that is not how a schema should be managed in production
(no versioning, no review, `update` cannot rename/drop).

## Decision

- **PostgreSQL profile:** the schema is owned by **Flyway** (versioned migrations
  under `db/migration`), and Hibernate runs with `ddl-auto=validate` — it only
  checks that the schema matches the entities.
- **H2 dev/test:** Flyway is disabled; Hibernate builds the schema with
  `create-drop` (plus a `schema.sql` for the sequence). This keeps tests fast and
  throwaway.
- The Flyway migrations are exercised against a **real PostgreSQL** in a
  Testcontainers integration test.

## Consequences

**Positive**

- Production-correct, versioned, reviewable schema; `validate` catches drift
  between entities and the actual database.
- Fast, disposable H2 for the bulk of the tests.
- Migrations are verified against the real engine (Testcontainers), not only H2.

**Negative / trade-offs**

- Two schema mechanisms coexist (Hibernate for H2, Flyway for Postgres); this is a
  deliberate, documented split.
- The migrations are not run against H2, so they are only validated by the
  Postgres integration test (acceptable, since Postgres is the real target).
