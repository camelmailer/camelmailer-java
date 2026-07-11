package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.emails.Attachment;
import com.camelmailer.emails.BatchItem;
import com.camelmailer.emails.Delivery;
import com.camelmailer.emails.EmailDetails;
import com.camelmailer.emails.EmailList;
import com.camelmailer.emails.ListEmailsOptions;
import com.camelmailer.emails.SendEmailRequest;
import com.camelmailer.emails.SendResult;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmailsTest {

  private static final String SEND_RESULT =
      "{\"message_id\":7,\"recipients\":[{\"rcpt_to\":\"ada@example.com\","
          + "\"message_id\":7,\"token\":\"tok123\",\"status\":\"queued\"}]}";

  @Test
  void sendPostsToMessagesAndParsesResult() {
    try (MockServer server = new MockServer()) {
      server.enqueue(201, "{\"status\":\"success\",\"time\":0.001,\"data\":" + SEND_RESULT + "}");
      SendResult result =
          server
              .client()
              .emails()
              .send(
                  SendEmailRequest.builder()
                      .from("billing@acme.com")
                      .to("ada@example.com")
                      .subject("Your receipt")
                      .textBody("Thanks for your purchase.")
                      .tag("receipt")
                      .build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/messages", recorded.path());
      assertTrue(recorded.body().contains("\"from\":\"billing@acme.com\""));
      assertTrue(recorded.body().contains("\"to\":[\"ada@example.com\"]"));
      assertTrue(recorded.body().contains("\"text_body\":\"Thanks for your purchase.\""));
      assertTrue(recorded.body().contains("\"tag\":\"receipt\""));
      assertFalse(recorded.body().contains("\"cc\""), "unset fields must be omitted");
      assertFalse(recorded.body().contains("\"template\""), "unset fields must be omitted");

      assertEquals(7L, result.messageId());
      assertEquals(1, result.recipients().size());
      assertEquals("ada@example.com", result.recipients().get(0).rcptTo());
      assertEquals("queued", result.recipients().get(0).status());
      assertEquals("tok123", result.recipients().get(0).token());
    }
  }

  @Test
  void sendSerializesNamedAddressesHeadersAndAttachments() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(SEND_RESULT);
      server
          .client()
          .emails()
          .send(
              SendEmailRequest.builder()
                  .from("hello@acme.com", "Acme Billing")
                  .to("ada@example.com")
                  .cc("grace@example.com")
                  .replyTo("support@acme.com")
                  .header("X-Custom", "1")
                  .attachment(
                      Attachment.of(
                          "invoice.pdf",
                          "application/pdf",
                          "PDFDATA".getBytes(StandardCharsets.UTF_8)))
                  .metadata(Map.of("order_id", 4711))
                  .stream("receipts")
                  .build());

      String body = server.takeRequest().body();
      assertTrue(
          body.contains("\"from\":{\"email\":\"hello@acme.com\",\"name\":\"Acme Billing\"}"));
      assertTrue(body.contains("\"cc\":[\"grace@example.com\"]"));
      assertTrue(body.contains("\"reply_to\":[\"support@acme.com\"]"));
      assertTrue(body.contains("\"headers\":{\"X-Custom\":\"1\"}"));
      assertTrue(body.contains("\"name\":\"invoice.pdf\""));
      assertTrue(body.contains("\"content_type\":\"application/pdf\""));
      assertTrue(body.contains("\"data_base64\":\"UERGREFUQQ==\""));
      assertTrue(body.contains("\"metadata\":{\"order_id\":4711}"));
      assertTrue(body.contains("\"stream\":\"receipts\""));
    }
  }

  @Test
  void sendBatchPostsRawArrayAndParsesMixedResults() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"messages\":[{\"status\":\"success\",\"data\":"
              + SEND_RESULT
              + "},{\"status\":\"error\",\"error\":{\"code\":\"ValidationError\","
              + "\"message\":\"missing recipient\"}}]}");
      List<BatchItem> results =
          server
              .client()
              .emails()
              .sendBatch(
                  SendEmailRequest.builder().from("a@acme.com").to("b@example.com").build(),
                  SendEmailRequest.builder().from("a@acme.com").build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/messages/batch", recorded.path());
      assertTrue(recorded.body().startsWith("["), "batch body must be a raw JSON array");

      assertEquals(2, results.size());
      assertTrue(results.get(0).isSuccess());
      assertEquals(7L, results.get(0).data().messageId());
      assertFalse(results.get(1).isSuccess());
      assertNull(results.get(1).data());
      assertEquals("ValidationError", results.get(1).error().code());
    }
  }

  @Test
  void sendWithTemplateIncludesTemplateAndModel() {
    try (MockServer server = new MockServer()) {
      server.enqueue(201, "{\"status\":\"success\",\"time\":0.001,\"data\":" + SEND_RESULT + "}");
      server
          .client()
          .emails()
          .sendWithTemplate(
              SendEmailRequest.builder()
                  .from("hello@acme.com")
                  .to("ada@example.com")
                  .template("welcome")
                  .templateModel(Map.of("name", "Ada"))
                  .build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/messages/with_template", recorded.path());
      assertTrue(recorded.body().contains("\"template\":\"welcome\""));
      assertTrue(recorded.body().contains("\"template_model\":{\"name\":\"Ada\"}"));
    }
  }

  @Test
  void sendWithTemplateBatchPostsRawArray() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"messages\":[{\"status\":\"success\",\"data\":" + SEND_RESULT + "}]}");
      List<BatchItem> results =
          server
              .client()
              .emails()
              .sendWithTemplateBatch(
                  List.of(
                      SendEmailRequest.builder()
                          .from("hello@acme.com")
                          .to("ada@example.com")
                          .template("welcome")
                          .build()));

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/messages/with_template/batch", recorded.path());
      assertTrue(recorded.body().startsWith("["));
      assertEquals(1, results.size());
      assertTrue(results.get(0).isSuccess());
    }
  }

  @Test
  void getParsesMessageAndDeliveries() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"message\":{\"id\":9,\"token\":\"tok\",\"scope\":\"outgoing\","
              + "\"rcpt_to\":\"ada@example.com\",\"subject\":\"Hi\",\"status\":\"Sent\","
              + "\"bounce\":false,\"held\":false,\"threat\":false,\"bypassed\":false,"
              + "\"created_at\":\"2026-07-11T09:00:00Z\"},"
              + "\"deliveries\":[{\"id\":1,\"status\":\"Sent\",\"details\":\"250 OK\","
              + "\"sent_with_ssl\":true,\"created_at\":\"2026-07-11T09:00:01Z\"}]}");
      EmailDetails details = server.client().emails().get(9);

      assertEquals("GET", server.takeRequest().method());
      assertEquals(9L, details.message().id());
      assertEquals("outgoing", details.message().scope());
      assertEquals("Sent", details.message().status());
      assertEquals(1, details.deliveries().size());
      assertEquals(Boolean.TRUE, details.deliveries().get(0).sentWithSsl());
    }
  }

  @Test
  void listBuildsFilterQueryAndParsesPage() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"messages\":[{\"id\":9,\"token\":\"tok\",\"scope\":\"outgoing\","
              + "\"rcpt_to\":\"ada@example.com\",\"bounce\":false,\"held\":false,"
              + "\"threat\":false,\"bypassed\":false,\"created_at\":\"2026-07-11T09:00:00Z\"}],"
              + "\"pagination\":{\"page\":2,\"per_page\":50,\"total\":51,\"total_pages\":2}}");
      EmailList list =
          server
              .client()
              .emails()
              .list(
                  ListEmailsOptions.builder()
                      .page(2)
                      .perPage(50)
                      .scope("outgoing")
                      .status("Sent")
                      .tag("receipt")
                      .query("ada@example.com")
                      .stream("default")
                      .build());

      String query = server.takeRequest().query();
      assertTrue(query.contains("page=2"));
      assertTrue(query.contains("per_page=50"));
      assertTrue(query.contains("scope=outgoing"));
      assertTrue(query.contains("status=Sent"));
      assertTrue(query.contains("tag=receipt"));
      assertTrue(query.contains("query=ada%40example.com"));
      assertTrue(query.contains("stream=default"));

      assertEquals(1, list.messages().size());
      assertEquals(2, list.pagination().page());
      assertEquals(51, list.pagination().total());
      assertEquals(2, list.pagination().totalPages());
    }
  }

  @Test
  void deliveriesParsesList() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"deliveries\":[{\"id\":1,\"status\":\"SoftFail\",\"output\":\"421 retry\","
              + "\"sent_with_ssl\":false,\"created_at\":\"2026-07-11T09:00:01Z\"}]}");
      List<Delivery> deliveries = server.client().emails().deliveries(9);
      assertEquals("/api/v2/server/messages/9/deliveries", server.takeRequest().path());
      assertEquals("SoftFail", deliveries.get(0).status());
    }
  }

  @Test
  void opensAndClicksParseActivityEvents() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"opens\":[{\"ip_address\":\"203.0.113.9\",\"user_agent\":\"Mozilla\","
              + "\"created_at\":\"2026-07-11T09:05:00Z\"}]}");
      server.enqueueData(
          "{\"clicks\":[{\"ip_address\":\"203.0.113.9\",\"url\":\"https://acme.com\","
              + "\"created_at\":\"2026-07-11T09:06:00Z\"}]}");

      assertEquals("203.0.113.9", server.client().emails().opens(9).get(0).ipAddress());
      assertEquals("/api/v2/server/messages/9/opens", server.takeRequest().path());

      assertEquals("https://acme.com", server.client().emails().clicks(9).get(0).url());
      assertEquals("/api/v2/server/messages/9/clicks", server.takeRequest().path());
    }
  }

  @Test
  void rawReturnsMessageSource() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"raw_message\":\"From: a@b.c\\r\\nSubject: Hi\"}");
      String raw = server.client().emails().raw(9);
      assertEquals("/api/v2/server/messages/9/raw", server.takeRequest().path());
      assertTrue(raw.startsWith("From: a@b.c"));
    }
  }
}
