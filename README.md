# camelmailer-java

[![CI](https://github.com/camelmailer/camelmailer-java/actions/workflows/ci.yml/badge.svg)](https://github.com/camelmailer/camelmailer-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

CamelMailer's official Java SDK — send transactional email via the
[CamelMailer](https://camelmailer.com) API.

Java 17+. One runtime dependency (Jackson databind); HTTP via the JDK's
`java.net.http`.

## Install

```xml
<dependency>
  <groupId>com.camelmailer</groupId>
  <artifactId>camelmailer-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle: `implementation("com.camelmailer:camelmailer-java:0.1.0")`

## Quickstart

```java
import com.camelmailer.CamelMailer;
import com.camelmailer.emails.SendEmailRequest;
import com.camelmailer.emails.SendResult;

CamelMailer client = new CamelMailer("cm_xxxx");

SendResult result = client.emails().send(
    SendEmailRequest.builder()
        .from("billing@acme.com")
        .to("ada@example.com")
        .subject("Your receipt")
        .htmlBody("<p>Thanks for your purchase.</p>")
        .build());
```

## Usage

### Emails

```java
// Send with all the trimmings
client.emails().send(
    SendEmailRequest.builder()
        .from("hello@acme.com", "Acme")           // display name
        .to("ada@example.com")
        .cc("grace@example.com")
        .replyTo("support@acme.com")
        .subject("Hello")
        .textBody("Hi!")
        .header("X-Campaign", "onboarding")
        .attachment(Attachment.of("invoice.pdf", "application/pdf", pdfBytes))
        .tag("welcome")
        .metadata(Map.of("user_id", 42))
        .stream("transactional")                   // message stream permalink
        .build());

// Batch — one result per message, individually inspectable
List<BatchItem> results = client.emails().sendBatch(requestA, requestB);
results.get(0).isSuccess();

// Stored template (Mustache-style {{ variables }})
client.emails().sendWithTemplate(
    SendEmailRequest.builder()
        .from("hello@acme.com")
        .to("ada@example.com")
        .template("welcome")
        .templateModel(Map.of("name", "Ada"))
        .build());
client.emails().sendWithTemplateBatch(List.of(...));

// Read back
EmailDetails details = client.emails().get(messageId);   // message + deliveries
EmailList page = client.emails().list(
    ListEmailsOptions.builder().scope("outgoing").tag("welcome").page(1).perPage(50).build());
List<Delivery> deliveries = client.emails().deliveries(messageId);
List<ActivityEvent> opens = client.emails().opens(messageId);
List<ActivityEvent> clicks = client.emails().clicks(messageId);
String raw = client.emails().raw(messageId);              // RFC 5322 source
```

### Templates

```java
Template created = client.templates().create(
    TemplateRequest.builder()
        .name("Welcome")
        .subject("Hi {{ name }}")
        .htmlBody("<p>Hi {{ name }}</p>")
        .build());
client.templates().list();
client.templates().get("welcome");
client.templates().update("welcome", TemplateRequest.builder().subject("Hello {{ name }}").build());
client.templates().archive("welcome");
RenderedTemplate preview = client.templates().render("welcome", Map.of("name", "Ada"));
```

### Streams

```java
client.streams().list();
client.streams().create(StreamRequest.builder().name("Receipts").streamType("transactional").build());
client.streams().get("receipts");
client.streams().update("receipts", StreamRequest.builder().name("Billing").build());
client.streams().archive("receipts");
```

### Stats and bounces

```java
MessageStats stats = client.stats().get();                             // all time
MessageStats window = client.stats().get("2026-01-01T00:00:00Z", null); // RFC 3339 window
DeliveryStats queue = client.stats().deliveries();                     // queue depth per domain

BounceList bounces = client.bounces().list(1, 50);
Email bounce = client.bounces().get(bounceId);
```

### DMARC

```java
DmarcSummary summary = client.dmarc().summary(
    DmarcQuery.builder().domain("acme.com").from("2026-07-01T00:00:00Z").build());
DmarcReportList reports = client.dmarc().reports();
DmarcReportDetail detail = client.dmarc().report(reportId);
```

## Error handling

API failures throw the unchecked `CamelMailerException`, carrying the API's
stable error code and the HTTP status:

```java
try {
  client.emails().send(request);
} catch (CamelMailerException e) {
  e.getCode();       // "ValidationError", "Unauthorized", "NotFound", ...
  e.getStatusCode(); // 422, 401, 404, ... (0 for connection errors)
  e.getMessage();    // human-readable detail
}
```

## Self-hosted instances

```java
CamelMailer client = CamelMailer.builder()
    .apiKey("cm_xxxx")
    .baseUrl("https://mail.example.com")
    .httpClient(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) // optional
    .build();
```

## Docs

Full API documentation: [camelmailer.com/docs](https://camelmailer.com/docs)

## License

[MIT](LICENSE)
