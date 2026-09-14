package com.camelmailer.logs;

import com.camelmailer.http.ApiClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The server's own request log and tag index: {@code /api/v2/server/logs} and {@code
 * /api/v2/server/tags}.
 *
 * <p>Useful when a send did not arrive and the question is whether the request ever reached the
 * API, and with what answer.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#logs()}.
 */
public final class Logs {

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Logs(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists logged API requests, newest first.
   *
   * @return the first page
   */
  public LogList list() {
    return list(null, null);
  }

  /**
   * Lists logged API requests with paging.
   *
   * @param page 1-based page number, or {@code null} for the first
   * @param perPage page size (capped at 100), or {@code null} for the default
   * @return the matching page
   */
  public LogList list(Integer page, Integer perPage) {
    Map<String, String> query = new LinkedHashMap<>();
    if (page != null) {
      query.put("page", String.valueOf(page));
    }
    if (perPage != null) {
      query.put("per_page", String.valueOf(perPage));
    }
    return client.convert(client.get("/api/v2/server/logs", query), LogList.class);
  }

  /**
   * Lists the tags used by the server's recent messages, most used first.
   *
   * @return the tags with their counts
   */
  public List<TagCount> tags() {
    return client.convertList(client.get("/api/v2/server/tags").path("tags"), TagCount.class);
  }
}
