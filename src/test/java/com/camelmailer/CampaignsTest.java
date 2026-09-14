package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.campaigns.Campaign;
import com.camelmailer.campaigns.CampaignDetail;
import com.camelmailer.campaigns.DraftCampaignRequest;
import com.camelmailer.campaigns.SendCampaignRequest;
import com.camelmailer.campaigns.UpdateCampaignRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignsTest {

  private static final String CAMPAIGN =
      "{\"id\":7,\"name\":\"September\",\"subject\":\"What shipped\",\"from\":\"news@acme.com\","
          + "\"status\":\"draft\",\"total\":120,\"sent\":0,\"stream_id\":3,"
          + "\"stream\":{\"permalink\":\"product-news\",\"name\":\"Product news\"},"
          + "\"scheduled_at\":null,\"created_at\":\"2026-09-01T10:00:00Z\",\"completed_at\":null}";

  @Test
  void listParsesCampaignsWithTheirStream() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaigns\":[" + CAMPAIGN + "]}");
      List<Campaign> campaigns = server.client().campaigns().list();

      assertEquals("/api/v2/server/campaigns", server.takeRequest().path());
      assertEquals(1, campaigns.size());
      assertEquals("product-news", campaigns.get(0).stream().permalink());
      assertEquals("draft", campaigns.get(0).status());
    }
  }

  @Test
  void listForStreamUsesTheStreamPath() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaigns\":[]}");
      server.client().campaigns().listForStream("product-news");
      assertEquals("/api/v2/server/streams/product-news/campaigns", server.takeRequest().path());
    }
  }

  @Test
  void getCarriesTheStats() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"campaign\":"
              + CAMPAIGN
              + ",\"stats\":{\"total\":120,\"sent\":118,\"delivered\":110,\"failed\":8,"
              + "\"opened\":40,\"clicked\":9,\"unsubscribed\":1}}");
      CampaignDetail detail = server.client().campaigns().get(7);

      assertEquals("/api/v2/server/campaigns/7", server.takeRequest().path());
      assertEquals(110L, detail.stats().delivered());
      assertEquals(7L, detail.campaign().id());
    }
  }

  @Test
  void getForStreamUsesTheStreamPath() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaign\":" + CAMPAIGN + "}");
      server.client().campaigns().getForStream("product-news", 7);
      assertEquals("/api/v2/server/streams/product-news/campaigns/7", server.takeRequest().path());
    }
  }

  @Test
  void createDraftNamesTheStreamInTheBody() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201, "{\"status\":\"success\",\"time\":0.001,\"data\":{\"campaign\":" + CAMPAIGN + "}}");
      Campaign campaign =
          server
              .client()
              .campaigns()
              .createDraft(
                  DraftCampaignRequest.builder().stream("product-news")
                      .from("news@acme.com")
                      .name("September")
                      .build());

      MockServer.Recorded recorded = server.takeRequest();
      // The planning route: the stream travels in the body, not the path.
      assertEquals("/api/v2/server/campaigns", recorded.path());
      assertTrue(recorded.body().contains("\"stream\":\"product-news\""));
      assertFalse(recorded.body().contains("scheduled_at"));
      assertEquals("draft", campaign.status());
    }
  }

  @Test
  void createDraftArmsASchedule() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201,
          "{\"status\":\"success\",\"time\":0.001,\"data\":{\"campaign\":"
              + "{\"id\":8,\"status\":\"scheduled\"}}}");
      Campaign campaign =
          server
              .client()
              .campaigns()
              .createDraft(
                  DraftCampaignRequest.builder().stream("product-news")
                      .from("news@acme.com")
                      .scheduledAt("2026-10-01T08:00:00Z")
                      .build());

      assertTrue(server.takeRequest().body().contains("\"scheduled_at\":\"2026-10-01T08:00:00Z\""));
      assertEquals("scheduled", campaign.status());
    }
  }

  @Test
  void createAndSendGoesOutImmediately() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201,
          "{\"status\":\"success\",\"time\":0.001,\"data\":{\"campaign\":"
              + "{\"id\":9,\"status\":\"sending\"}}}");
      Campaign campaign =
          server
              .client()
              .campaigns()
              .createAndSend(
                  "product-news",
                  SendCampaignRequest.builder()
                      .name("Status update")
                      .from("news@acme.com")
                      .build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/streams/product-news/campaigns", recorded.path());
      // The stream-scoped route expands to the subscribers before it answers.
      assertEquals("sending", campaign.status());
    }
  }

  @Test
  void updateSchedulesWithATime() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaign\":{\"id\":7,\"status\":\"scheduled\"}}");
      server
          .client()
          .campaigns()
          .update(7, UpdateCampaignRequest.builder().scheduledAt("2026-10-01T08:00:00Z").build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("PATCH", recorded.method());
      assertTrue(recorded.body().contains("\"scheduled_at\":\"2026-10-01T08:00:00Z\""));
    }
  }

  @Test
  void clearScheduleSendsAnExplicitNull() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaign\":{\"id\":7,\"status\":\"draft\"}}");
      Campaign campaign =
          server
              .client()
              .campaigns()
              .update(7, UpdateCampaignRequest.builder().clearSchedule().build());

      // An omitted field leaves the schedule standing; only an explicit
      // null drops the campaign back to a draft.
      assertTrue(server.takeRequest().body().contains("\"scheduled_at\":null"));
      assertEquals("draft", campaign.status());
    }
  }

  @Test
  void anUntouchedScheduleIsOmitted() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaign\":" + CAMPAIGN + "}");
      server
          .client()
          .campaigns()
          .update(7, UpdateCampaignRequest.builder().subject("Corrected").build());

      String body = server.takeRequest().body();
      assertTrue(body.contains("\"subject\":\"Corrected\""));
      assertFalse(body.contains("scheduled_at"));
    }
  }

  @Test
  void sendAndCancel() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"campaign\":{\"id\":7,\"status\":\"sending\"}}");
      server.enqueueData("{\"campaign\":{\"id\":7,\"status\":\"canceled\"}}");

      assertEquals("sending", server.client().campaigns().send(7).status());
      assertEquals("/api/v2/server/campaigns/7/send", server.takeRequest().path());
      assertEquals("canceled", server.client().campaigns().cancel(7).status());
      assertEquals("/api/v2/server/campaigns/7/cancel", server.takeRequest().path());
    }
  }

  @Test
  void editingASendingCampaignIsRefused() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          422,
          "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\"ValidationError\","
              + "\"message\":\"a sent campaign can no longer be edited\"}}");

      CamelMailerException error =
          assertThrows(
              CamelMailerException.class,
              () ->
                  server
                      .client()
                      .campaigns()
                      .update(7, UpdateCampaignRequest.builder().subject("Too late").build()));
      assertEquals("ValidationError", error.getCode());
    }
  }
}
