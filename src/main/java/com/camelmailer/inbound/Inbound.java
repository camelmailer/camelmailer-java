package com.camelmailer.inbound;

import com.camelmailer.emails.Email;
import com.camelmailer.http.ApiClient;
import java.util.Map;

/**
 * Inbound and held messages: {@code /api/v2/server/inbound}.
 *
 * <p>Covers mail arriving through an inbound route as well as outbound mail the spam filter put on
 * hold, which is why a message here can be either retried or released past the hold.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#inbound()}.
 */
public final class Inbound {

  private static final String BASE = "/api/v2/server/inbound";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Inbound(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists inbound and held messages, newest first.
   *
   * @return the first page, unfiltered
   */
  public InboundList list() {
    return client.convert(client.get(BASE, Map.of()), InboundList.class);
  }

  /**
   * Lists inbound and held messages with filters.
   *
   * @param options the filters
   * @return the matching page
   */
  public InboundList list(ListInboundOptions options) {
    return client.convert(client.get(BASE, options.toQuery()), InboundList.class);
  }

  /**
   * Fetches one inbound message.
   *
   * @param id the message id
   * @return the message
   */
  public Email get(long id) {
    return client.convert(client.get(BASE + "/" + id).path("message"), Email.class);
  }

  /**
   * Puts a message back on the delivery queue, for instance after fixing the route it should have
   * matched.
   *
   * @param id the message id
   * @return whether it was queued
   */
  public RequeueResult retry(long id) {
    return client.convert(client.post(BASE + "/" + id + "/retry", null), RequeueResult.class);
  }

  /**
   * Releases a held message past the hold and delivers it.
   *
   * @param id the message id
   * @return whether it was queued
   */
  public RequeueResult bypass(long id) {
    return client.convert(client.post(BASE + "/" + id + "/bypass", null), RequeueResult.class);
  }
}
