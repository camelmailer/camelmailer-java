# camelmailer-java

[![CI](https://github.com/camelmailer/camelmailer-java/actions/workflows/ci.yml/badge.svg)](https://github.com/camelmailer/camelmailer-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Camelmailer's official Java SDK — send transactional email via the
[Camelmailer](https://camelmailer.com) API.

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

// Retry-safe sends: the same key with the same body returns the first
// result instead of queuing a second copy, and a different body under the
// same key is refused with InvalidIdempotentRequest. All four send methods
// take one.
client.emails().send(request, "order-" + orderId);

// Broadcast to everyone subscribed to a stream. Recipients past the
// per-request cap of 1000 come back as skipped, so a larger audience wants
// a campaign.
StreamSendResult broadcast = client.emails().sendToStream("newsletter",
    SendEmailRequest.builder()
        .from("news@acme.com")
        .subject("September")
        .textBody("What shipped this month.")
        .build());

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
client.streams().create(StreamRequest.builder()
    .name("Receipts").permalink("receipts").streamType("transactional").build());
client.streams().get("receipts");
client.streams().update("receipts", StreamRequest.builder().name("Billing").build());
client.streams().archive("receipts");
```

### Campaigns

A campaign is content plus an audience. The two ways to create one behave
differently, so pick deliberately: `createDraft` writes it and waits,
`createAndSend` expands it to the stream's subscribers before the call
returns.

```java
// Write it and leave it alone. Without a schedule it stays a draft; with
// one it becomes "scheduled" and the server sends it when due.
Campaign draft = client.campaigns().createDraft(
    DraftCampaignRequest.builder()
        .stream("newsletter")
        .from("news@acme.com")
        .name("September")
        .subject("What shipped")
        .textBody("Hello.")
        // .scheduledAt("2026-10-01T08:00:00Z")
        .build());

// Goes out on the spot, no draft and no schedule.
client.campaigns().createAndSend("newsletter",
    SendCampaignRequest.builder()
        .name("Status update")
        .from("news@acme.com")
        .textBody("All clear.")
        .build());

client.campaigns().list();
client.campaigns().listForStream("newsletter");
CampaignDetail detail = client.campaigns().get(draft.id());   // campaign + stats

// scheduledAt schedules; clearSchedule drops it back to a draft. Touching
// neither leaves the schedule standing, so the two are separate.
client.campaigns().update(draft.id(),
    UpdateCampaignRequest.builder().scheduledAt("2026-10-01T08:00:00Z").build());
client.campaigns().update(draft.id(),
    UpdateCampaignRequest.builder().clearSchedule().build());

client.campaigns().send(draft.id());      // now, whatever the schedule said
client.campaigns().cancel(draft.id());
```

### Subscribers

A broadcast send to an address that is not subscribed is refused, so this
list is the audience.

```java
client.subscribers().list("newsletter");
client.subscribers().add("newsletter",
    SubscriberRequest.builder().address("ada@example.com").build());
client.subscribers().importAddresses("newsletter",
    List.of("ada@example.com", "grace@example.com"));
client.subscribers().complaint("newsletter", "ada@example.com");  // suppress + unsubscribe
client.subscribers().remove("newsletter", "ada@example.com");
```

### Layouts

A layout wraps every template that uses it. `htmlWrapper` has to embed the
body with `{{{ content }}}`.

```java
client.layouts().list();
client.layouts().create(LayoutRequest.builder()
    .name("Default")
    .permalink("default")
    .htmlWrapper("<html><body>{{{ content }}}</body></html>")
    .build());
client.layouts().get("default");
client.layouts().update("default", LayoutRequest.builder().name("Main").build());
String logoUrl = client.layouts().uploadLogo("default", "data:image/png;base64,...");
client.layouts().delete("default");
```

### Inbound and held messages

```java
InboundList held = client.inbound().list(
    ListInboundOptions.builder().status("held").build());
client.inbound().get(55);
client.inbound().retry(55).requeued();    // back on the delivery queue
client.inbound().bypass(55).requeued();   // release past the hold
```

### Logs

Useful when a send did not arrive and the question is whether the request
ever reached the API.

```java
LogList requests = client.logs().list(null, 25);
List<TagCount> tags = client.logs().tags();
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
