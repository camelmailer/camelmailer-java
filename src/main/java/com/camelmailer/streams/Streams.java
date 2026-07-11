package com.camelmailer.streams;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Manage message streams: {@code /api/v2/server/streams}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#streams()}.
 */
public final class Streams {

  private static final String BASE = "/api/v2/server/streams";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Streams(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists all message streams.
   *
   * @return the streams
   */
  public List<MessageStream> list() {
    JsonNode data = client.get(BASE);
    return client.convertList(data.path("streams"), MessageStream.class);
  }

  /**
   * Creates a message stream.
   *
   * @param request stream fields; name is required
   * @return the created stream
   */
  public MessageStream create(StreamRequest request) {
    return client.convert(client.post(BASE, request).path("stream"), MessageStream.class);
  }

  /**
   * Fetches a stream.
   *
   * @param permalink the stream permalink
   * @return the stream
   */
  public MessageStream get(String permalink) {
    return client.convert(client.get(BASE + "/" + permalink).path("stream"), MessageStream.class);
  }

  /**
   * Updates a stream.
   *
   * @param permalink the stream permalink
   * @param request the fields to change
   * @return the updated stream
   */
  public MessageStream update(String permalink, StreamRequest request) {
    return client.convert(
        client.patch(BASE + "/" + permalink, request).path("stream"), MessageStream.class);
  }

  /**
   * Archives a stream.
   *
   * @param permalink the stream permalink
   * @return the archived stream
   */
  public MessageStream archive(String permalink) {
    return client.convert(
        client.post(BASE + "/" + permalink + "/archive", null).path("stream"), MessageStream.class);
  }
}
