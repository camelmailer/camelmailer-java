package com.camelmailer.dmarc;

import com.camelmailer.http.ApiClient;

/**
 * DMARC compliance reporting: {@code /api/v2/server/dmarc}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#dmarc()}.
 */
public final class Dmarc {

  private static final String BASE = "/api/v2/server/dmarc";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Dmarc(ApiClient client) {
    this.client = client;
  }

  /**
   * Fetches the DMARC compliance summary over all stored reports.
   *
   * @return the summary
   */
  public DmarcSummary summary() {
    return summary(DmarcQuery.builder().build());
  }

  /**
   * Fetches the DMARC compliance summary, filtered by domain and/or time window.
   *
   * @param query filters
   * @return the summary
   */
  public DmarcSummary summary(DmarcQuery query) {
    return client.convert(
        client.get(BASE + "/summary", query.toQuery()).path("summary"), DmarcSummary.class);
  }

  /**
   * Lists stored DMARC aggregate reports, newest report range first.
   *
   * @return the first page of reports
   */
  public DmarcReportList reports() {
    return reports(DmarcQuery.builder().build());
  }

  /**
   * Lists stored DMARC aggregate reports with filters and pagination.
   *
   * @param query filters and pagination
   * @return a page of reports
   */
  public DmarcReportList reports(DmarcQuery query) {
    return client.convert(client.get(BASE + "/reports", query.toQuery()), DmarcReportList.class);
  }

  /**
   * Fetches one report with its records.
   *
   * @param id the report id
   * @return report and records
   */
  public DmarcReportDetail report(long id) {
    return client.convert(client.get(BASE + "/reports/" + id), DmarcReportDetail.class);
  }
}
