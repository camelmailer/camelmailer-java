# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.1] - 2026-09-14

### Fixed

- `inbound` retry and bypass read `queued`. The endpoint answers with
  `requeued`, so both returned false and no error whatever happened. They
  also expose the `message` the response carries.
- The subscriber types carried a `name`. The endpoint takes an address and a
  status; a name was silently dropped, so the field promised something the
  API does not store.

## [0.2.0] - 2026-09-14

### Added

- `campaigns()`: `createDraft`, `createAndSend`, `list`, `listForStream`,
  `get`, `getForStream`, `update`, `send`, `cancel`. The two create methods
  hit different routes: `createDraft` writes the campaign and waits, while
  `createAndSend` expands it to the stream's subscribers before the call
  returns.
- `subscribers()`: `list`, `add`, `importAddresses`, `complaint`, `remove`.
- `layouts()`: `list`, `create`, `get`, `update`, `delete`, `uploadLogo`.
- `inbound()`: `list`, `get`, `retry`, `bypass`.
- `logs()`: `list`, `tags`.
- `emails().sendToStream()` for broadcasting to a stream's subscribers.
- An idempotency-key overload on all four send methods. The key travels as
  the `Idempotency-Key` header, because the body is what the server hashes
  to recognise a replay.
- `StreamRequest` takes a `permalink` and an `archived` flag. Without the
  permalink the API derives one from the name, which a caller that has to
  know the permalink up front cannot rely on.

## [0.1.0] - 2026-07-12

### Added

- Initial release.
- `CamelMailer` client with builder (`baseUrl` for self-hosted instances, custom `HttpClient`).
- `emails()`: `send`, `sendBatch`, `sendWithTemplate`, `sendWithTemplateBatch`, `get`, `list`
  (scope/status/tag/query/stream filters), `deliveries`, `opens`, `clicks`, `raw`.
- `templates()`: `list`, `create`, `get`, `update`, `archive`, `render`.
- `streams()`: `list`, `create`, `get`, `update`, `archive`.
- `stats()`: `get` (optional time window), `deliveries`.
- `bounces()`: `list`, `get`.
- `dmarc()`: `summary`, `reports`, `report`.
- Typed, unchecked `CamelMailerException` with stable API error `code` and HTTP `statusCode`.

[Unreleased]: https://github.com/camelmailer/camelmailer-java/compare/v0.2.1...HEAD
[0.2.1]: https://github.com/camelmailer/camelmailer-java/releases/tag/v0.2.1
[0.2.0]: https://github.com/camelmailer/camelmailer-java/releases/tag/v0.2.0
[0.1.0]: https://github.com/camelmailer/camelmailer-java/releases/tag/v0.1.0
