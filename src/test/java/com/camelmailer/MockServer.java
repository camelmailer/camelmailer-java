package com.camelmailer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/** A tiny in-process HTTP stub built on the JDK's {@link HttpServer}. No test-infra deps. */
final class MockServer implements AutoCloseable {

  /** A recorded incoming request. */
  record Recorded(
      String method, String path, String rawPath, String query, Headers headers, String body) {}

  private record Stubbed(int status, String body) {}

  private final HttpServer server;
  private final Deque<Stubbed> responses = new ArrayDeque<>();
  private final Deque<Recorded> requests = new ArrayDeque<>();

  MockServer() {
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    server.createContext(
        "/",
        exchange -> {
          String body =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          requests.add(
              new Recorded(
                  exchange.getRequestMethod(),
                  exchange.getRequestURI().getPath(),
                  // The raw path keeps the percent-encoding, which is the
                  // only place an escaped address can be checked.
                  exchange.getRequestURI().getRawPath(),
                  exchange.getRequestURI().getRawQuery(),
                  exchange.getRequestHeaders(),
                  body));
          Stubbed stubbed = responses.poll();
          int status = stubbed == null ? 200 : stubbed.status();
          String payload =
              stubbed == null
                  ? "{\"status\":\"success\",\"time\":0.001,\"data\":{}}"
                  : stubbed.body();
          byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, bytes.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    server.start();
  }

  /** Queues a raw response. */
  void enqueue(int status, String body) {
    responses.add(new Stubbed(status, body));
  }

  /** Queues a 200 success envelope wrapping the given {@code data} JSON. */
  void enqueueData(String dataJson) {
    enqueue(200, "{\"status\":\"success\",\"time\":0.001,\"data\":" + dataJson + "}");
  }

  /** Queues an error envelope with the given HTTP status, code, and message. */
  void enqueueError(int status, String code, String message) {
    enqueue(
        status,
        "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\""
            + code
            + "\",\"message\":\""
            + message
            + "\"}}");
  }

  /** Returns the base URL of the stub. */
  String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  /** Returns a client wired to this stub. */
  CamelMailer client() {
    return CamelMailer.builder().apiKey("cm_test_key").baseUrl(baseUrl()).build();
  }

  /** Takes the oldest recorded request. */
  Recorded takeRequest() {
    Recorded recorded = requests.poll();
    if (recorded == null) {
      throw new IllegalStateException("no request was recorded");
    }
    return recorded;
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
