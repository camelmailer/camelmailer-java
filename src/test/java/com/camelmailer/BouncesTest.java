package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.bounces.BounceList;
import com.camelmailer.emails.Email;
import org.junit.jupiter.api.Test;

class BouncesTest {

  private static final String BOUNCE =
      "{\"id\":11,\"token\":\"tok11\",\"scope\":\"outgoing\",\"rcpt_to\":\"gone@example.com\","
          + "\"subject\":\"Hi\",\"status\":\"HardFail\",\"bounce\":true,\"held\":false,"
          + "\"threat\":false,\"bypassed\":false,\"created_at\":\"2026-07-11T09:00:00Z\"}";

  @Test
  void listParsesBouncesAndPagination() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"bounces\":["
              + BOUNCE
              + "],\"pagination\":{\"page\":1,\"per_page\":30,\"total\":1,\"total_pages\":1}}");
      BounceList list = server.client().bounces().list();

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("GET", recorded.method());
      assertEquals("/api/v2/server/bounces", recorded.path());
      assertNull(recorded.query());

      assertEquals(1, list.bounces().size());
      assertTrue(list.bounces().get(0).bounce());
      assertEquals("gone@example.com", list.bounces().get(0).rcptTo());
      assertEquals(1, list.pagination().page());
    }
  }

  @Test
  void listSendsPaginationParams() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"bounces\":[],\"pagination\":{\"page\":3,\"per_page\":10,\"total\":0,\"total_pages\":0}}");
      server.client().bounces().list(3, 10);

      String query = server.takeRequest().query();
      assertTrue(query.contains("page=3"));
      assertTrue(query.contains("per_page=10"));
    }
  }

  @Test
  void getParsesSingleBounce() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"bounce\":" + BOUNCE + "}");
      Email bounce = server.client().bounces().get(11);

      assertEquals("/api/v2/server/bounces/11", server.takeRequest().path());
      assertEquals(11L, bounce.id());
      assertEquals("HardFail", bounce.status());
    }
  }

  @Test
  void notFoundIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(404, "NotFound", "bounce not found");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().bounces().get(999));
      assertEquals("NotFound", e.getCode());
      assertEquals(404, e.getStatusCode());
    }
  }
}
