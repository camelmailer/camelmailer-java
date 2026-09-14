package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.inbound.InboundList;
import com.camelmailer.inbound.ListInboundOptions;
import com.camelmailer.logs.LogList;
import com.camelmailer.logs.TagCount;
import java.util.List;
import org.junit.jupiter.api.Test;

class InboundLogsTest {

  @Test
  void inboundListReadsTheInboundKey() {
    try (MockServer server = new MockServer()) {
      // The page comes back under "inbound", not "messages".
      server.enqueueData(
          "{\"inbound\":[{\"id\":55,\"status\":\"Held\",\"held\":true}],"
              + "\"pagination\":{\"page\":1,\"per_page\":50,\"total\":1,\"total_pages\":1}}");
      InboundList page =
          server
              .client()
              .inbound()
              .list(ListInboundOptions.builder().status("held").perPage(50).build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/inbound", recorded.path());
      assertTrue(recorded.query().contains("status=held"), "query was " + recorded.query());
      assertTrue(recorded.query().contains("per_page=50"), "query was " + recorded.query());
      assertEquals(1, page.inbound().size());
      assertEquals(1L, page.pagination().total());
    }
  }

  @Test
  void inboundRetryAndBypass() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"queued\":true}");
      assertTrue(server.client().inbound().retry(55).queued());
      assertEquals("/api/v2/server/inbound/55/retry", server.takeRequest().path());

      server.enqueueData("{\"queued\":true}");
      assertTrue(server.client().inbound().bypass(55).queued());
      assertEquals("/api/v2/server/inbound/55/bypass", server.takeRequest().path());
    }
  }

  @Test
  void logsListAndTags() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"requests\":[{\"id\":1,\"method\":\"POST\",\"path\":\"/api/v2/server/messages\","
              + "\"status_code\":201,\"duration_ms\":12,\"user_agent\":\"camelmailer-java\","
              + "\"created_at\":\"2026-09-14T08:00:00Z\"}],"
              + "\"pagination\":{\"page\":1,\"per_page\":25,\"total\":1,\"total_pages\":1}}");
      LogList page = server.client().logs().list(null, 25);

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/logs", recorded.path());
      assertTrue(recorded.query().contains("per_page=25"));
      assertEquals(201, page.requests().get(0).statusCode());
      assertEquals(12L, page.requests().get(0).durationMs());

      server.enqueueData("{\"tags\":[{\"tag\":\"receipt\",\"count\":12}]}");
      List<TagCount> tags = server.client().logs().tags();
      assertEquals("/api/v2/server/tags", server.takeRequest().path());
      assertEquals("receipt", tags.get(0).tag());
      assertEquals(12L, tags.get(0).count());
    }
  }
}
