package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.stats.DeliveryStats;
import com.camelmailer.stats.MessageStats;
import org.junit.jupiter.api.Test;

class StatsTest {

  @Test
  void getParsesAllCounters() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"stats\":{\"total\":100,\"incoming\":20,\"outgoing\":80,\"sent\":70,"
              + "\"pending\":5,\"held\":1,\"bounced\":4,\"soft_fail\":2,\"hard_fail\":2,"
              + "\"opens\":40,\"clicks\":10,\"unique_opens\":30,\"unique_clicks\":8}}");
      MessageStats stats = server.client().stats().get();

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("GET", recorded.method());
      assertEquals("/api/v2/server/stats", recorded.path());
      assertNull(recorded.query(), "no window params expected");

      assertEquals(100, stats.total());
      assertEquals(20, stats.incoming());
      assertEquals(80, stats.outgoing());
      assertEquals(70, stats.sent());
      assertEquals(5, stats.pending());
      assertEquals(1, stats.held());
      assertEquals(4, stats.bounced());
      assertEquals(2, stats.softFail());
      assertEquals(2, stats.hardFail());
      assertEquals(40, stats.opens());
      assertEquals(10, stats.clicks());
      assertEquals(30, stats.uniqueOpens());
      assertEquals(8, stats.uniqueClicks());
    }
  }

  @Test
  void getWithWindowSendsFromAndTo() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"stats\":{\"total\":1}}");
      server.client().stats().get("2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z");

      String query = server.takeRequest().query();
      assertTrue(query.contains("from=2026-01-01T00%3A00%3A00Z"));
      assertTrue(query.contains("to=2026-02-01T00%3A00%3A00Z"));
    }
  }

  @Test
  void deliveriesParsesQueueDepths() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"queued\":12,\"domains\":[{\"domain\":\"gmail.com\",\"queued\":9},"
              + "{\"domain\":\"example.com\",\"queued\":3}]}");
      DeliveryStats stats = server.client().stats().deliveries();

      assertEquals("/api/v2/server/stats/deliveries", server.takeRequest().path());
      assertEquals(12, stats.queued());
      assertEquals(2, stats.domains().size());
      assertEquals("gmail.com", stats.domains().get(0).domain());
      assertEquals(9, stats.domains().get(0).queued());
    }
  }

  @Test
  void unauthorizedIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(401, "Unauthorized", "invalid API key");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().stats().deliveries());
      assertEquals("Unauthorized", e.getCode());
      assertEquals(401, e.getStatusCode());
    }
  }
}
