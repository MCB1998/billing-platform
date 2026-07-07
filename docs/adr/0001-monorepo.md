# 1. Monorepo for all services

Date: 2026-07-07

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

The billing platform will consist of several microservices (customer, invoice,
notification, api-gateway). I had to decide whether to keep each service in its
own Git repository (multi-repo / polyrepo) or all of them in a single repository
(monorepo). This is a solo, portfolio oriented project.

## Decision

Use a single **monorepo**: all services live as subfolders inside one repository
(`billing-platform`).

## Consequences

**Positive**

- One repository to showcase the whole system — a single entry point for reviewers.
- Cross-service changes (e.g. a shared API contract) can be done in one commit / PR.
- A single `docker-compose.yml` and one CI workflow cover the whole platform.
- Low overhead for a single developer.

**Negative / trade-offs**

- Coarser access control and shared versioning (irrelevant for a solo project).
- CI must target the relevant service subfolders as the platform grows.

**Note:** A monorepo is a *code storage* decision, not a runtime one. Each service
stays an independent microservice (own build, own container image, own database,
independently deployable) — a monorepo is **not** a monolith.
