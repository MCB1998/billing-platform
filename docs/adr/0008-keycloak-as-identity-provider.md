# 8. Keycloak as identity provider, gateway as OAuth2 resource server

Date: 2026-07-17

## Status

Accepted (who wants to oppose you if you're alone hehe)

## Context

Until now the platform was completely open: anyone who could reach a service could
create customers or issue invoices. A billing back office needs authentication.

Two questions had to be answered:

1. **Where is authentication enforced?** Building it into customer-, invoice- and
   notification-service would mean implementing the same concern three times and
   keeping the three copies in sync.
2. **Who issues and verifies credentials?** Either we build our own login and sign our
   own tokens, or we delegate to a dedicated identity provider (IdP).

Rolling our own would mean storing passwords, hashing them correctly, handling resets,
lockouts and token lifetimes — a large, security-critical surface that is entirely
undifferentiated work for a billing system.

## Decision

Authentication is delegated to **Keycloak** and enforced **once, at the api-gateway**.

- **Keycloak = authorization server / IdP.** It owns users and passwords and issues
  signed JWTs. It is the only component that ever sees a credential.
- **api-gateway = OAuth2 resource server.** It only *validates* tokens. It issues none,
  has no login page and performs no redirects — a caller arrives with a token already.
  Requests without a valid token are rejected with `401` before any proxying happens.
- **The services behind the gateway stay free of authentication code.**
- Tokens are signed **asymmetrically (RS256)**: Keycloak signs with its private key, the
  gateway verifies with the matching public key fetched from Keycloak's JWK Set (the
  token's `kid` header selects the key). Gateway and Keycloak share **no secret**.
- **The realm is configuration-as-code:** `keycloak/realm-billing.json` defines realm,
  client and a test user, and is re-imported on every container start.

Scope of this decision is **authentication** only (is the caller known?). Authorization
by roles is a separate, later step.

## Consequences

**Positive**

- One place to enforce authentication instead of three; the services stay simple.
- No password ever reaches our code — the entire credential surface lives in Keycloak.
- Standard OAuth2/OIDC: swapping Keycloak for another compliant IdP is a config change,
  and enterprise realities (LDAP/AD federation, social login, MFA) are Keycloak features
  rather than things we would have to build.
- The realm is reproducible and reviewable: `docker compose up` always yields the same
  setup, because Keycloak dev mode keeps no state.

**Negative / trade-offs**

- A new, non-trivial piece of infrastructure to run and understand.
- Local development now needs Keycloak running to obtain a token (the gateway itself
  still starts without it, see [ADR-0009](0009-jwk-set-uri-over-issuer-uri.md)).
- The realm file enables the **direct access grant** (password grant) so a token can be
  fetched with a single `curl`. That grant is discouraged for real clients in OAuth 2.1;
  it is a deliberate convenience for local demos and tests only.
- Dev mode uses an in-memory database and ships a known test password — this setup is
  explicitly **not** production-shaped. A real deployment needs a persistent database,
  real user management (or AD federation) and no committed credentials.
