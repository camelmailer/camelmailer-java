# Contributing

## Development setup

- JDK 17+ and Maven 3.9+.
- Build and test: `mvn verify`
- Format (google-java-format via Spotless): `mvn spotless:apply`

## Tests

Unit tests mock the HTTP layer with the JDK's built-in `HttpServer` — no
network, no extra test dependencies. Every resource and error path is covered;
new behaviour lands with tests first.

The integration test (`IntegrationTest`) runs only when `CAMELMAILER_API_KEY`
is set (optionally `CAMELMAILER_BASE_URL`, `CAMELMAILER_FROM`,
`CAMELMAILER_TO`) and is not part of CI.

## Conventions

- Public API carries Javadoc; `mvn verify` builds the Javadoc jar and fails on doclint errors.
- Requests are immutable builder POJOs, responses are records.
- Only runtime dependency is Jackson databind; HTTP stays on `java.net.http`.
- Keep commits small and focused. CI (`ci.yml`) must be green.
