package com.camelmailer.stats;

import com.camelmailer.http.ApiClient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message and delivery statistics: {@code /api/v2/server/stats}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#stats()}.
 */
public final class Stats {

  private static final String BASE = "/api/v2/server/stats";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Stats(ApiClient client) {
    this.client = client;
  }

  /**
   * Fetches aggregate message counters over all time.
   *
   * @return the counters
   */
  public MessageStats get() {
    return get(null, null);
  }

  /**
   * Fetches aggregate message counters within a time window.
   *
   * @param from window start (RFC 3339, e.g. {@code 2026-01-01T00:00:00Z}), or {@code null}
   * @param to window end (RFC 3339), or {@code null}
   * @return the counters
   */
  public MessageStats get(String from, String to) {
    Map<String, String> query = new LinkedHashMap<>();
    query.put("from", from);
    query.put("to", to);
    return client.convert(client.get(BASE, query).path("stats"), MessageStats.class);
  }

  /**
   * Fetches the pending outbound queue depth, in total and per recipient domain.
   *
   * @return the delivery statistics
   */
  public DeliveryStats deliveries() {
    return client.convert(client.get(BASE + "/deliveries"), DeliveryStats.class);
  }
}
