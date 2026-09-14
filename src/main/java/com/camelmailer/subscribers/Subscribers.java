package com.camelmailer.subscribers;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Opt-in subscribers of a broadcast stream: {@code /api/v2/server/streams/{permalink}/subscribers}.
 *
 * <p>A broadcast send to an address that is not subscribed is refused, so this list is the
 * audience.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#subscribers()}.
 */
public final class Subscribers {

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Subscribers(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists the stream's subscribers, subscribed and unsubscribed alike.
   *
   * @param permalink the stream permalink
   * @return the subscribers
   */
  public List<Subscriber> list(String permalink) {
    return client.convertList(client.get(base(permalink)).path("subscribers"), Subscriber.class);
  }

  /**
   * Adds or updates one subscriber. Upserts by address, so calling it twice is safe.
   *
   * <p>The endpoint takes an address and a status; there is no name field.
   *
   * @param permalink the stream permalink
   * @param request the subscriber fields
   * @return the stored subscriber
   */
  public Subscriber add(String permalink, SubscriberRequest request) {
    return client.convert(
        client.post(base(permalink), request).path("subscriber"), Subscriber.class);
  }

  /**
   * Adds many addresses at once, all as subscribed.
   *
   * @param permalink the stream permalink
   * @param addresses the addresses to subscribe
   * @return how many were written, and the resulting total
   */
  public ImportResult importAddresses(String permalink, List<String> addresses) {
    ObjectNode body = client.newObject();
    addresses.forEach(body.putArray("addresses")::add);
    return client.convert(client.post(base(permalink) + "/import", body), ImportResult.class);
  }

  /**
   * Records a spam complaint against an address.
   *
   * <p>Writes a stream-scoped suppression and flips the subscription to {@code unsubscribed}.
   * Idempotent, so a feedback loop can replay it safely.
   *
   * @param permalink the stream permalink
   * @param address the complaining address
   * @return the updated subscriber
   */
  public Subscriber complaint(String permalink, String address) {
    return client.convert(
        client
            .post(base(permalink) + "/" + encode(address) + "/complaint", null)
            .path("subscriber"),
        Subscriber.class);
  }

  /**
   * Removes a subscriber from the stream entirely.
   *
   * @param permalink the stream permalink
   * @param address the address to remove
   * @return whether the row was removed
   */
  public boolean remove(String permalink, String address) {
    return client.delete(base(permalink) + "/" + encode(address)).path("deleted").asBoolean();
  }

  private static String base(String permalink) {
    return "/api/v2/server/streams/" + permalink + "/subscribers";
  }

  /**
   * Percent-encodes an address for a path segment.
   *
   * <p>{@link URLEncoder} is form encoding, which turns a space into {@code +} and leaves a literal
   * {@code +} alone; in a path a bare plus is ambiguous, so it is escaped explicitly.
   */
  private static String encode(String address) {
    return URLEncoder.encode(address, StandardCharsets.UTF_8).replace("+", "%2B");
  }
}
