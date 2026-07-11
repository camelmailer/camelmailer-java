package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class CamelMailerTest {

  @Test
  void defaultBaseUrlIsTheCloud() {
    assertEquals("https://app.camelmailer.com", CamelMailer.DEFAULT_BASE_URL);
  }

  @Test
  void constructorRejectsNullApiKey() {
    assertThrows(IllegalArgumentException.class, () -> new CamelMailer(null));
  }

  @Test
  void builderRejectsBlankApiKey() {
    assertThrows(IllegalArgumentException.class, () -> CamelMailer.builder().apiKey("  ").build());
  }

  @Test
  void builderRejectsMissingApiKey() {
    assertThrows(IllegalArgumentException.class, () -> CamelMailer.builder().build());
  }

  @Test
  void servicesAreAvailable() {
    CamelMailer client = new CamelMailer("cm_test_key");
    assertNotNull(client.emails());
    assertNotNull(client.templates());
    assertNotNull(client.streams());
    assertNotNull(client.stats());
    assertNotNull(client.bounces());
    assertNotNull(client.dmarc());
  }

  @Test
  void customBaseUrlIsUsedAndTrailingSlashIsStripped() {
    try (MockServer server = new MockServer()) {
      CamelMailer client =
          CamelMailer.builder().apiKey("cm_test_key").baseUrl(server.baseUrl() + "/").build();
      client.stats().get();
      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/stats", recorded.path());
    }
  }

  @Test
  void apiKeyIsSentAsServerApiKeyHeader() {
    try (MockServer server = new MockServer()) {
      server.client().stats().get();
      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("cm_test_key", recorded.headers().getFirst("X-Server-Api-Key"));
      assertEquals("application/json", recorded.headers().getFirst("Content-Type"));
    }
  }

  @Test
  void customHttpClientIsUsed() {
    try (MockServer server = new MockServer()) {
      HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
      CamelMailer client =
          CamelMailer.builder()
              .apiKey("cm_test_key")
              .baseUrl(server.baseUrl())
              .httpClient(http)
              .build();
      client.stats().get();
      assertEquals("/api/v2/server/stats", server.takeRequest().path());
    }
  }
}
