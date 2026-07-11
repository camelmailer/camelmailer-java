package com.camelmailer.bounces;

import com.camelmailer.emails.Email;
import com.camelmailer.http.ApiClient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read bounced messages: {@code /api/v2/server/bounces}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#bounces()}.
 */
public final class Bounces {

  private static final String BASE = "/api/v2/server/bounces";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Bounces(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists bounced messages, newest first.
   *
   * @return the first page of bounces
   */
  public BounceList list() {
    return list(null, null);
  }

  /**
   * Lists bounced messages with pagination.
   *
   * @param page page number (1-based), or {@code null}
   * @param perPage items per page (max 100), or {@code null}
   * @return a page of bounces
   */
  public BounceList list(Integer page, Integer perPage) {
    Map<String, String> query = new LinkedHashMap<>();
    query.put("page", page == null ? null : page.toString());
    query.put("per_page", perPage == null ? null : perPage.toString());
    return client.convert(client.get(BASE, query), BounceList.class);
  }

  /**
   * Fetches a single bounce.
   *
   * @param id the message id
   * @return the bounced message
   */
  public Email get(long id) {
    return client.convert(client.get(BASE + "/" + id).path("bounce"), Email.class);
  }
}
