# 9. Configure the resource server with jwk-set-uri instead of issuer-uri

Date: 2026-07-17

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

To validate JWTs, the gateway's resource server ([ADR-0008](0008-keycloak-as-identity-provider.md))
needs Keycloak's public keys. Spring Boot offers two ways to configure this:

- **`issuer-uri`** — point at the realm. At **startup**, Spring performs OIDC discovery:
  it calls the realm's `.well-known/openid-configuration`, learns the JWK Set location,
  and additionally validates the `iss` claim of every token from then on.
- **`jwk-set-uri`** — point directly at the JWK Set endpoint. The keys are fetched
  **lazily**, on the first token that needs verifying.

The difference matters beyond style: with `issuer-uri`, the gateway **cannot start** if
Keycloak is unreachable, because the discovery call happens while the bean is created.
That couples the gateway's startup to the IdP's availability — including in tests and
CI, where no Keycloak runs at all.

## Decision

Configure the resource server with **`jwk-set-uri`**, pointing at Keycloak's
`protocol/openid-connect/certs` endpoint. The value is externalized under
`keycloak.jwk-set-uri` so it can be overridden per environment.

Signature and expiry are still validated on every token — only the automatic `iss`
check and the discovery step are given up.

## Consequences

**Positive**

- The gateway starts independently of Keycloak; the IdP only has to be up when a token
  actually needs verifying.
- Tests and CI run fully offline: unauthenticated requests are rejected before any key
  is needed, and authenticated cases are simulated with `mockJwt()`. No Keycloak
  container in the pipeline, no flakiness from a slow IdP startup.
- One less startup-order dependency in local development.

**Negative / trade-offs**

- The `iss` claim is **not** validated automatically. Signature verification already ties
  a token to Keycloak's keys, so the practical exposure is small, but `issuer-uri` would
  have given this check for free.
- The JWK Set location is hard-coded in configuration rather than discovered. If Keycloak
  ever moved that endpoint, the config would have to follow.
- If stricter validation is wanted later, the options are to switch to `issuer-uri` and
  accept the startup coupling, or to add an explicit issuer validator to the decoder.
