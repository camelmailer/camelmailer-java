# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/camelmailer/camelmailer-java/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/camelmailer/camelmailer-java/releases/tag/v0.1.0
