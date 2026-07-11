package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.camelmailer.emails.EmailDetails;
import com.camelmailer.emails.SendEmailRequest;
import com.camelmailer.emails.SendResult;
import com.camelmailer.stats.MessageStats;
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
