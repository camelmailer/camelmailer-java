package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.streams.MessageStream;
import com.camelmailer.streams.StreamRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class StreamsTest {

  private static final String STREAM =
      "{\"id\":2,\"uuid\":\"u-2\",\"name\":\"Receipts\",\"permalink\":\"receipts\","
          + "\"stream_type\":\"transactional\",\"archived\":false}";

  @Test
  void listParsesStreams() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"streams\":[" + STREAM + "]}");
      List<MessageStream> streams = server.client().streams().list();

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("GET", recorded.method());
      assertEquals("/api/v2/server/streams", recorded.path());
      assertEquals(1, streams.size());
      assertEquals("receipts", streams.get(0).permalink());
      assertEquals("transactional", streams.get(0).streamType());
    }
  }

  @Test
  void createPostsNameAndStreamType() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201, "{\"status\":\"success\",\"time\":0.001,\"data\":{\"stream\":" + STREAM + "}}");
      MessageStream stream =
          server
              .client()
              .streams()
              .create(StreamRequest.builder().name("Receipts").streamType("transactional").build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/streams", recorded.path());
      assertTrue(recorded.body().contains("\"name\":\"Receipts\""));
      assertTrue(recorded.body().contains("\"stream_type\":\"transactional\""));
      assertEquals(2L, stream.id());
    }
  }

  @Test
  void getFetchesByPermalink() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"stream\":" + STREAM + "}");
      MessageStream stream = server.client().streams().get("receipts");
      assertEquals("/api/v2/server/streams/receipts", server.takeRequest().path());
      assertEquals("Receipts", stream.name());
    }
  }

  @Test
  void updatePatchesStream() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"stream\":" + STREAM + "}");
      server.client().streams().update("receipts", StreamRequest.builder().name("Billing").build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("PATCH", recorded.method());
      assertEquals("/api/v2/server/streams/receipts", recorded.path());
      assertTrue(recorded.body().contains("\"name\":\"Billing\""));
    }
  }

  @Test
  void archivePostsToArchive() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"stream\":{\"id\":2,\"uuid\":\"u-2\",\"name\":\"Receipts\","
              + "\"permalink\":\"receipts\",\"stream_type\":\"transactional\",\"archived\":true}}");
      MessageStream stream = server.client().streams().archive("receipts");

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/streams/receipts/archive", recorded.path());
      assertTrue(stream.archived());
    }
  }

  @Test
  void validationErrorOnCreateIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(422, "ValidationError", "stream_type must be transactional or broadcast");
      CamelMailerException e =
          assertThrows(
              CamelMailerException.class,
              () ->
                  server
                      .client()
                      .streams()
                      .create(StreamRequest.builder().name("X").streamType("bogus").build()));
      assertEquals("ValidationError", e.getCode());
      assertEquals(422, e.getStatusCode());
    }
  }
}
