# 3. Server-generated customer numbers via a database sequence

Date: 2026-07-07

## Status

Accepted (who wants to oppose you if you're alone hehe)


## Context

A customer has a human-readable business key `customerNumber` (e.g. `C-00001`),
separate from the technical database `id`. I had to decide who assigns it and
how to keep it unique — safely, even under concurrent requests and multiple
running service instances.

Naive approaches are unsafe:

- `count() + 1`: two concurrent inserts can pick the same number (race condition),
  and numbers get reused after (soft-)deletes.
- An in-memory counter / `synchronized` block: only coordinates within a single
  JVM and breaks as soon as the service is scaled to multiple instances.

## Decision

The **service generates** the customer number from a dedicated **database
sequence** (`customer_number_seq`), formatted as `C-%05d`. The database is the
single point of coordination, so generation is atomic across concurrent requests
and across instances. A unique constraint on `customer_number` is a second line
of defense.

Client-provided numbers (needed for importing existing customers from a legacy
system) are intentionally deferred to a future iteration as this project will most likely never see any customer data migrations :D

## Consequences

**Positive**

- Unique, consistent, concurrency-safe numbering across all instances.
- The numbering rule is encapsulated in one place; callers don't need to know it.

**Negative / trade-offs**

- The sequence may leave gaps (a rolled-back transaction consumes a value). This
  is acceptable: uniqueness is guaranteed, gap-free numbering is not required.
- Importing pre-existing customer numbers is not yet supported.
