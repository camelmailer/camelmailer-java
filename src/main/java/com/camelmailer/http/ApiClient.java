package com.camelmailer.http;

import com.camelmailer.CamelMailerException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internal HTTP layer of the SDK: builds requests, applies authentication, and unwraps the {@code
 * {status, time, data | error}} envelope every CamelMailer response uses.
 *
 * <p>Not intended for direct use by SDK consumers; the surface may change without notice.
 */
public final class ApiClient {

  /** SDK version, sent in the User-Agent header. Keep in sync with the POM. */
  public static final String VERSION = "0.2.1";

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final HttpClient http;
  private final String baseUrl;
  private final String apiKey;

  /**
   * Creates the client.
   *
   * @param http the underlying JDK HTTP client
   * @param baseUrl instance base URL, without trailing slash
   * @param apiKey server API key sent as {@code X-Server-API-Key}
   */
  public ApiClient(HttpClient http, String baseUrl, String apiKey) {
    this.http = http;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.apiKey = apiKey;
  }

  /**
   * Performs a GET request.
   *
   * @param path absolute API path, e.g. {@code /api/v2/server/messages}
   * @return the {@code data} node of the success envelope
   */
  public JsonNode get(String path) {
    return get(path, Map.of());
  }

  /**
   * Performs a GET request with query parameters.
   *
   * @param path absolute API path
   * @param query query parameters; {@code null} values are skipped
   * @return the {@code data} node of the success envelope
   */
  public JsonNode get(String path, Map<String, String> query) {
    return execute(request(path, query).GET().build());
  }

  /**
   * Performs a POST request.
   *
   * @param path absolute API path
   * @param body request body, serialized as JSON; {@code null} sends an empty JSON object
   * @return the {@code data} node of the success envelope
   */
  public JsonNode post(String path, Object body) {
    return post(path, body, null);
  }

  /**
   * Performs a POST request with an idempotency key.
   *
   * <p>The key travels as the {@code Idempotency-Key} header rather than in the body, because the
   * body is what the server hashes to recognise the same request.
   *
   * @param path absolute API path
   * @param body request body, serialized as JSON; {@code null} sends an empty JSON object
   * @param idempotencyKey the key, or {@code null} to send none
   * @return the {@code data} node of the success envelope
   */
  public JsonNode post(String path, Object body, String idempotencyKey) {
    HttpRequest.Builder builder = request(path, Map.of());
    if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
      builder = builder.header("Idempotency-Key", idempotencyKey);
    }
    return execute(builder.POST(jsonBody(body)).build());
  }

  /**
   * Performs a DELETE request.
   *
   * @param path absolute API path
   * @return the {@code data} node of the success envelope
   */
  public JsonNode delete(String path) {
    return execute(request(path, Map.of()).DELETE().build());
  }

  /**
   * Performs a PATCH request.
   *
   * @param path absolute API path
   * @param body request body, serialized as JSON
   * @return the {@code data} node of the success envelope
   */
  public JsonNode patch(String path, Object body) {
    return execute(request(path, Map.of()).method("PATCH", jsonBody(body)).build());
  }

  /**
   * Maps a JSON node to a typed value.
   *
   * @param <T> target type
   * @param node source node
   * @param type target class
   * @return the mapped value; {@code null} when the node is missing or {@code null}
   */
  public <T> T convert(JsonNode node, Class<T> type) {
    if (node == null || node.isNull() || node.isMissingNode()) {
      return null;
    }
    try {
      return MAPPER.treeToValue(node, type);
    } catch (JsonProcessingException e) {
      throw new CamelMailerException("InvalidResponse", e.getMessage(), 0, e);
    }
  }

  /**
   * Maps a JSON array node to a typed list.
   *
   * @param <T> element type
   * @param node source array node
   * @param type element class
   * @return the mapped list; empty when the node is missing or {@code null}
   */
  public <T> List<T> convertList(JsonNode node, Class<T> type) {
    List<T> result = new ArrayList<>();
    if (node == null || node.isNull() || node.isMissingNode()) {
      return result;
    }
    for (JsonNode element : node) {
      result.add(convert(element, type));
    }
    return result;
  }

  /**
   * Creates a mutable JSON object for a request body.
   *
   * <p>Needed where a field has to reach the API as an explicit {@code null}: the mapper drops null
   * properties, but a {@code NullNode} placed here survives.
   *
   * @return an empty object node
   */
  public ObjectNode newObject() {
    return MAPPER.createObjectNode();
  }

  private HttpRequest.Builder request(String path, Map<String, String> query) {
    StringBuilder url = new StringBuilder(baseUrl).append(path);
    boolean first = true;
    for (Map.Entry<String, String> entry : query.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      url.append(first ? '?' : '&')
          .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
          .append('=')
          .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      first = false;
    }
    return HttpRequest.newBuilder(URI.create(url.toString()))
        .header("X-Server-API-Key", apiKey)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("User-Agent", "camelmailer-java/" + VERSION);
  }

  private HttpRequest.BodyPublisher jsonBody(Object body) {
    try {
      String json = body == null ? "{}" : MAPPER.writeValueAsString(body);
      return HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
    } catch (JsonProcessingException e) {
      throw new CamelMailerException("InvalidRequest", e.getMessage(), 0, e);
    }
  }

  private JsonNode execute(HttpRequest request) {
    HttpResponse<String> response;
    try {
      response = http.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new CamelMailerException("ConnectionError", e.getMessage(), 0, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CamelMailerException("ConnectionError", "request interrupted", 0, e);
    }

    JsonNode root;
    try {
      root = MAPPER.readTree(response.body());
    } catch (JsonProcessingException e) {
      throw new CamelMailerException(
          "InvalidResponse",
          "could not parse response body (HTTP " + response.statusCode() + ")",
          response.statusCode(),
          e);
    }

    boolean errorEnvelope = "error".equals(root.path("status").asText());
    if (response.statusCode() >= 400 || errorEnvelope) {
      JsonNode error = root.path("error");
      String code = error.path("code").asText();
      String message = error.path("message").asText();
      if (code.isEmpty()) {
        code = "HttpError";
      }
      if (message.isEmpty()) {
        message = "request failed with HTTP " + response.statusCode();
      }
      throw new CamelMailerException(code, message, response.statusCode());
    }
    return root.path("data");
  }
}
