# 7. Kotlin for the notification-service

Date: 2026-07-11

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

The other services are written in Java. The notification-service is small,
event-driven and mostly data classes plus a couple of components — a good, low-risk
place to introduce a second JVM language. Kotlin runs on the same JVM, uses the same
Spring Boot starters, and interoperates with the rest of the platform at the wire
level (events/JSON), so nothing else has to change.

## Decision

The notification-service is written in **Kotlin**; the other services stay in Java.

- The build uses the `kotlin-maven-plugin` with the **`spring`** compiler plugin
  (makes Spring-managed classes `open`, since Kotlin classes are `final` by default)
  and the **`jpa`** plugin (gives `@Entity` classes a no-arg constructor and opens
  them) — this resolves the usual Kotlin/JPA friction.
- `jackson-module-kotlin` is registered so Kotlin `data class` events deserialize
  correctly, and the RabbitMQ converter reuses Spring Boot's configured `ObjectMapper`
  (which also has the JavaTime module) so `LocalDate`/`BigDecimal` round-trip.

## Consequences

**Positive**

- Concise domain code: `data class` events and `val`-only entities express the
  write-once notification log with far less boilerplate than Java.
- Null-safety at the type level fits an event consumer that parses external messages.
- Demonstrates polyglot-on-the-JVM: the services integrate through events, not shared
  code, so language choice is a per-service decision.

**Negative / trade-offs**

- A second language in the repo: contributors need both, and the build carries extra
  Kotlin compiler-plugin configuration.
- Kotlin/JPA needs the `no-arg`/`all-open` plugins to work at all — a sharp edge that
  has to be understood and documented.
- Tooling that assumes Java (e.g. some Mockito usage) needs the Kotlin-friendly
  variant (`mockito-kotlin`) for idiomatic tests.
