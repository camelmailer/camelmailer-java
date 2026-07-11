package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.dmarc.DmarcQuery;
import com.camelmailer.dmarc.DmarcReportDetail;
import com.camelmailer.dmarc.DmarcReportList;
import com.camelmailer.dmarc.DmarcSummary;
import org.junit.jupiter.api.Test;

class DmarcTest {

  private static final String REPORT =
      "{\"id\":5,\"domain\":\"acme.com\",\"org_name\":\"google.com\","
          + "\"org_email\":\"noreply-dmarc@google.com\",\"report_id\":\"r-1\","
          + "\"date_range_begin\":\"2026-07-10T00:00:00Z\","
          + "\"date_range_end\":\"2026-07-11T00:00:00Z\","
          + "\"received_at\":\"2026-07-11T04:00:00Z\",\"record_count\":2}";

  @Test
  void summaryParsesComplianceData() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"summary\":{\"total\":100,\"pass\":90,\"fail\":10,\"pass_rate\":0.9,"
              + "\"by_source\":[{\"source_ip\":\"203.0.113.10\",\"count\":60,"
              + "\"spf_aligned_pct\":100.0,\"dkim_aligned_pct\":95.0,"
              + "\"disposition_counts\":{\"none\":60}}],"
              + "\"by_disposition\":{\"none\":95,\"quarantine\":5}}}");
      DmarcSummary summary = server.client().dmarc().summary();

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("GET", recorded.method());
      assertEquals("/api/v2/server/dmarc/summary", recorded.path());
      assertNull(recorded.query());

      assertEquals(100, summary.total());
      assertEquals(90, summary.pass());
      assertEquals(10, summary.fail());
      assertEquals(0.9, summary.passRate());
      assertEquals(1, summary.bySource().size());
      assertEquals("203.0.113.10", summary.bySource().get(0).sourceIp());
      assertEquals(95.0, summary.bySource().get(0).dkimAlignedPct());
      assertEquals(60L, summary.bySource().get(0).dispositionCounts().get("none"));
      assertEquals(5L, summary.byDisposition().get("quarantine"));
    }
  }

  @Test
  void summarySendsDomainAndWindowFilters() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"summary\":{\"total\":0,\"pass\":0,\"fail\":0,\"pass_rate\":0.0}}");
      server
          .client()
          .dmarc()
          .summary(
              DmarcQuery.builder()
                  .domain("acme.com")
                  .from("2026-07-01T00:00:00Z")
                  .to("2026-07-11T00:00:00Z")
                  .build());

      String query = server.takeRequest().query();
      assertTrue(query.contains("domain=acme.com"));
      assertTrue(query.contains("from=2026-07-01T00%3A00%3A00Z"));
      assertTrue(query.contains("to=2026-07-11T00%3A00%3A00Z"));
    }
  }

  @Test
  void reportsParsesListAndPagination() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"reports\":["
              + REPORT
              + "],\"pagination\":{\"page\":2,\"per_page\":25,\"total\":26,\"total_pages\":2}}");
      DmarcReportList list =
          server.client().dmarc().reports(DmarcQuery.builder().page(2).perPage(25).build());

      String query = server.takeRequest().query();
      assertTrue(query.contains("page=2"));
      assertTrue(query.contains("per_page=25"));

      assertEquals(1, list.reports().size());
      assertEquals("acme.com", list.reports().get(0).domain());
      assertEquals(2, list.reports().get(0).recordCount());
      assertEquals(2, list.pagination().page());
    }
  }

  @Test
  void reportParsesDetailWithRecords() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"report\":"
              + REPORT
              + ",\"records\":[{\"id\":1,\"source_ip\":\"203.0.113.10\",\"count\":60,"
              + "\"disposition\":\"none\",\"dkim_result\":\"pass\",\"spf_result\":\"pass\","
              + "\"dkim_aligned\":true,\"spf_aligned\":true,\"header_from\":\"acme.com\","
              + "\"envelope_from\":\"bounce.acme.com\"}]}");
      DmarcReportDetail detail = server.client().dmarc().report(5);

      assertEquals("/api/v2/server/dmarc/reports/5", server.takeRequest().path());
      assertEquals(5L, detail.report().id());
      assertEquals(1, detail.records().size());
      assertTrue(detail.records().get(0).dkimAligned());
      assertEquals("none", detail.records().get(0).disposition());
    }
  }

  @Test
  void notFoundReportIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(404, "NotFound", "report not found");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().dmarc().report(404));
      assertEquals("NotFound", e.getCode());
      assertEquals(404, e.getStatusCode());
    }
  }
}
