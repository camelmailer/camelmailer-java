package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.campaigns.Campaign;
import com.camelmailer.campaigns.DraftCampaignRequest;
import com.camelmailer.campaigns.SendCampaignRequest;
import com.camelmailer.campaigns.UpdateCampaignRequest;
import com.camelmailer.emails.EmailDetails;
import com.camelmailer.emails.SendEmailRequest;
import com.camelmailer.emails.SendResult;
import com.camelmailer.emails.StreamSendResult;
import com.camelmailer.layouts.Layout;
import com.camelmailer.layouts.LayoutRequest;
import com.camelmailer.stats.MessageStats;
import com.camelmailer.streams.StreamRequest;
import com.camelmailer.subscribers.ImportResult;
import com.camelmailer.subscribers.SubscriberRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Round-trip test against a real CamelMailer instance. Skipped unless the environment provides:
 *
 * <ul>
 *   <li>{@code CAMELMAILER_API_KEY} — a server API key (required to run at all)
 *   <li>{@code CAMELMAILER_BASE_URL} — instance URL (optional, defaults to the cloud)
 *   <li>{@code CAMELMAILER_FROM} / {@code CAMELMAILER_TO} — addresses for the send test (optional;
 *       the send test is skipped without them)
 * </ul>
 *
 * <p>Not part of CI; run manually with {@code mvn test -Dtest=IntegrationTest}.
 */
@EnabledIfEnvironmentVariable(named = "CAMELMAILER_API_KEY", matches = ".+")
class IntegrationTest {

  private static CamelMailer client() {
    CamelMailer.Builder builder =
        CamelMailer.builder().apiKey(System.getenv("CAMELMAILER_API_KEY"));
    String baseUrl = System.getenv("CAMELMAILER_BASE_URL");
    if (baseUrl != null && !baseUrl.isBlank()) {
      builder.baseUrl(baseUrl);
    }
    return builder.build();
  }

  @Test
  void statsAndListsAreReachable() {
    CamelMailer client = client();

    MessageStats stats = client.stats().get();
    assertNotNull(stats);

    assertNotNull(client.emails().list());
    assertNotNull(client.streams().list());
    assertNotNull(client.templates().list());
    assertNotNull(client.bounces().list());
    assertNotNull(client.dmarc().summary());
    assertNotNull(client.campaigns().list());
    assertNotNull(client.layouts().list());
    assertNotNull(client.inbound().list());
    assertNotNull(client.logs().list());
    assertNotNull(client.logs().tags());
  }

  @Test
  @EnabledIfEnvironmentVariable(named = "CAMELMAILER_FROM", matches = ".+")
  void broadcastRoundTrip() {
    CamelMailer client = client();
    String stamp = String.valueOf(System.currentTimeMillis());
    String permalink = "java-live-" + stamp;
    String from = System.getenv("CAMELMAILER_FROM");

    String stream =
        client
            .streams()
            .create(
                StreamRequest.builder()
                    .name("Java live " + stamp)
                    .permalink(permalink)
                    .streamType("broadcast")
                    .build())
            .permalink();

    client
        .subscribers()
        .add(stream, SubscriberRequest.builder().address("ada@example.test").build());
    ImportResult imported =
        client.subscribers().importAddresses(stream, List.of("grace@example.test"));
    assertEquals(1L, imported.added());

    // The two create routes behave differently, which is the whole reason
    // they are separate methods.
    Campaign draft =
        client
            .campaigns()
            .createDraft(
                DraftCampaignRequest.builder().stream(stream)
                    .from(from)
                    .name("Java draft " + stamp)
                    .subject("D")
                    .textBody("d")
                    .build());
    assertEquals("draft", draft.status());

    Campaign scheduled =
        client
            .campaigns()
            .createDraft(
                DraftCampaignRequest.builder().stream(stream)
                    .from(from)
                    .name("Java scheduled " + stamp)
                    .scheduledAt("2027-01-01T09:00:00Z")
                    .build());
    assertEquals("scheduled", scheduled.status());

    Campaign sending =
        client
            .campaigns()
            .createAndSend(
                stream,
                SendCampaignRequest.builder()
                    .name("Java now " + stamp)
                    .from(from)
                    .subject("N")
                    .textBody("n")
                    .build());
    assertEquals("sending", sending.status());

    assertEquals(
        "scheduled",
        client
            .campaigns()
            .update(
                draft.id(),
                UpdateCampaignRequest.builder().scheduledAt("2027-01-01T09:00:00Z").build())
            .status());
    assertEquals(
        "draft",
        client
            .campaigns()
            .update(draft.id(), UpdateCampaignRequest.builder().clearSchedule().build())
            .status());
    assertEquals("canceled", client.campaigns().cancel(draft.id()).status());

    StreamSendResult broadcast =
        client
            .emails()
            .sendToStream(
                stream,
                SendEmailRequest.builder()
                    .from(from)
                    .subject("Broadcast " + stamp)
                    .textBody("hello")
                    .build());
    assertEquals(0L, broadcast.skipped());

    client.subscribers().remove(stream, "grace@example.test");
    client.streams().archive(stream);
  }

  @Test
  @EnabledIfEnvironmentVariable(named = "CAMELMAILER_FROM", matches = ".+")
  @EnabledIfEnvironmentVariable(named = "CAMELMAILER_TO", matches = ".+")
  void anIdempotentSendReplaysInsteadOfSendingTwice() {
    CamelMailer client = client();
    String key = "java-live-" + System.currentTimeMillis();
    SendEmailRequest request =
        SendEmailRequest.builder()
            .from(System.getenv("CAMELMAILER_FROM"))
            .to(System.getenv("CAMELMAILER_TO"))
            .subject("camelmailer-java idempotency")
            .textBody("once")
            .build();

    long first = client.emails().send(request, key).messageId();
    assertEquals(first, client.emails().send(request, key).messageId());

    SendEmailRequest other =
        SendEmailRequest.builder()
            .from(System.getenv("CAMELMAILER_FROM"))
            .to(System.getenv("CAMELMAILER_TO"))
            .subject("camelmailer-java idempotency (different)")
            .textBody("twice")
            .build();
    CamelMailerException error =
        assertThrows(CamelMailerException.class, () -> client.emails().send(other, key));
    assertEquals("InvalidIdempotentRequest", error.getCode());
  }

  @Test
  void layoutRoundTrip() {
    CamelMailer client = client();
    String permalink = "java-layout-" + System.currentTimeMillis();

    Layout created =
        client
            .layouts()
            .create(
                LayoutRequest.builder()
                    .name(permalink)
                    .permalink(permalink)
                    .htmlWrapper("<html><body>{{{ content }}}</body></html>")
                    .build());
    assertEquals(permalink, created.permalink());
    // A layout created with only an HTML wrapper has no text one.
    assertNull(created.textWrapper());

    String logo =
        client
            .layouts()
            .uploadLogo(
                permalink,
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ"
                    + "AAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
    // The endpoint answers with "url"; reading "logo_url" would yield "".
    assertFalse(logo.isEmpty());

    assertTrue(client.layouts().delete(permalink));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = "CAMELMAILER_FROM", matches = ".+")
  @EnabledIfEnvironmentVariable(named = "CAMELMAILER_TO", matches = ".+")
  void sendRoundTrip() {
    CamelMailer client = client();

    SendResult result =
        client
            .emails()
            .send(
                SendEmailRequest.builder()
                    .from(System.getenv("CAMELMAILER_FROM"))
                    .to(System.getenv("CAMELMAILER_TO"))
                    .subject("camelmailer-java integration test")
                    .textBody("Sent by the camelmailer-java integration test.")
                    .tag("sdk-integration-test")
                    .build());

    assertNotNull(result.messageId());
    assertFalse(result.recipients().isEmpty());

    EmailDetails details = client.emails().get(result.messageId());
    assertEquals("camelmailer-java integration test", details.message().subject());
  }
}
