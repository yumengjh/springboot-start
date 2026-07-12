# Authentication and session guide

## Contract

Authentication endpoints are `POST /api/v1/auth/register`, `login`, `refresh`, and
`logout`. Registration accepts `username`, `displayName`, and `password`; login accepts
`username` and `password`; refresh/logout accept `refreshToken`.

Registration accepts a username, display name, and password. Usernames match
`[A-Za-z0-9_.-]{3,32}` and normalize with `Locale.ROOT`; display names are 1–80
characters and passwords are 10–128 characters. Passwords are encoded through Spring
Security's `DelegatingPasswordEncoder` (initially BCrypt) and are never returned.

Login returns:

- a short-lived RSA SHA-256 signed JWT access token; and
- an opaque refresh token backed only by a SHA-256 hash in the database.

Access tokens expire after 15 minutes and refresh sessions expire after 30 days.

## Access tokens

Send the access token on authenticated requests:

```http
Authorization: Bearer <access-token>
```

JWT claims include issuer, user ID subject, username, effective permissions, token
version, issuance/expiry times, and a unique token ID. Access tokens are not persisted.
Permission changes become visible on the next issued access token; an already-issued
token retains its embedded authorities until it expires or is otherwise rejected.

## Refresh rotation and reuse

Every successful refresh consumes the presented token and returns a successor. Clients
must atomically replace the old value. The server stores only a hash, plus family,
user, expiry, consumption/revocation state, and sanitized client metadata.

If an already-consumed token is presented, the server returns the stable
`AUTHENTICATION_REQUIRED` and revokes every session in that token family. Treat this
as a possible credential theft event: discard the entire local session and require a
new login. Do not retry the stale token.

Current-device logout revokes the current refresh session/family as implemented.
All-device logout revokes all refresh sessions for the user. Administrator-forced
revocation, password changes, and account disablement also revoke sessions.

## RSA key configuration and rotation

Production uses an RSA private/public key pair supplied through configured values or
mounted secret files. No usable production key belongs in Git, an image layer, Compose,
logs, or documentation. Restrict private-key file access to the application identity;
only the public key may be distributed to verifiers.

Ephemeral RSA generation is local-only and requires both the `local` profile and
`app.security.jwt.allow-ephemeral-key=true`. Tokens become invalid after restart, so it
is unsuitable for shared or production environments.

For rotation:

1. Generate the new pair in the deployment's secret manager.
2. Arrange a verification overlap if the implemented key-selection configuration
   supports multiple public keys/key IDs; otherwise plan for existing access tokens to
   expire at cutover.
3. Deploy signers with the new private key and the required verification material.
4. After the maximum access-token lifetime, remove the old verification key.
5. Test login, access-token validation, and refresh before completing rollout.

The starter is not a full OAuth2 authorization server. Do not infer discovery, client
registration, authorization-code, or introspection endpoints.

## Client storage guidance

- Browser applications may prefer a Secure, HttpOnly, SameSite cookie for refresh
  tokens only when the server implements a compatible cookie transport or a
  backend-for-frontend performs refresh on the browser's behalf. Browser JavaScript
  cannot read an HttpOnly cookie to populate a JSON request. Otherwise, follow the
  actual OpenAPI token-transport contract. Keep access tokens short-lived and in
  memory; avoid `localStorage` for bearer credentials.
- Native clients should use platform secure storage (Keychain/Keystore equivalents).
- Never log tokens, put them in URLs, analytics, crash reports, or error messages.
- Serialize refresh operations per session. Concurrent use of one refresh token can
  make a legitimate request look like reuse and revoke the family.
- On `401`, distinguish an expired access token from refresh failure; allow at most one
  coordinated refresh before requiring login.
