package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.layouts.Layout;
import com.camelmailer.layouts.LayoutRequest;
import com.camelmailer.subscribers.ImportResult;
import com.camelmailer.subscribers.Subscriber;
import com.camelmailer.subscribers.SubscriberRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubscribersLayoutsTest {

  @Test
  void subscribersListParses() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"subscribers\":[{\"id\":1,\"address\":\"ada@example.com\","
              + "\"status\":\"subscribed\",\"created_at\":\"2026-09-01T10:00:00Z\"}]}");
      List<Subscriber> subscribers = server.client().subscribers().list("product-news");

      assertEquals("/api/v2/server/streams/product-news/subscribers", server.takeRequest().path());
      assertEquals("subscribed", subscribers.get(0).status());
    }
  }

  @Test
  void subscribersAddUpsertsByAddress() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201,
          "{\"status\":\"success\",\"time\":0.001,\"data\":{\"subscriber\":"
              + "{\"id\":1,\"address\":\"ada@example.com\",\"status\":\"subscribed\"}}}");
      Subscriber subscriber =
          server
              .client()
              .subscribers()
              .add(
                  "product-news",
                  SubscriberRequest.builder().address("ada@example.com").name("Ada").build());

      assertTrue(server.takeRequest().body().contains("\"address\":\"ada@example.com\""));
      assertEquals("ada@example.com", subscriber.address());
    }
  }

  @Test
  void subscribersImportSendsAnAddressesArray() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"added\":2,\"total\":2}");
      ImportResult result =
          server
              .client()
              .subscribers()
              .importAddresses("product-news", List.of("ada@example.com", "grace@example.com"));

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/streams/product-news/subscribers/import", recorded.path());
      assertTrue(recorded.body().contains("\"addresses\":[\"ada@example.com\""));
      assertEquals(2L, result.added());
    }
  }

  @Test
  void subscribersRemoveEscapesTheAddress() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"deleted\":true}");
      boolean removed =
          server.client().subscribers().remove("product-news", "ada+news@example.com");

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("DELETE", recorded.method());
      // The plus has to survive the path, or a different address is removed.
      assertTrue(
          recorded.rawPath().endsWith("/subscribers/ada%2Bnews%40example.com"),
          "raw path was " + recorded.rawPath());
      assertTrue(removed);
    }
  }

  @Test
  void subscribersComplaintUnsubscribes() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"subscriber\":{\"id\":1,\"address\":\"ada@example.com\",\"status\":\"unsubscribed\"}}");
      Subscriber subscriber =
          server.client().subscribers().complaint("product-news", "ada@example.com");

      assertTrue(server.takeRequest().path().endsWith("/complaint"));
      assertEquals("unsubscribed", subscriber.status());
    }
  }

  @Test
  void layoutsListParses() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"layouts\":[{\"id\":1,\"uuid\":\"l-1\",\"name\":\"Default\",\"permalink\":\"default\","
              + "\"html_wrapper\":\"<html>{{{ content }}}</html>\",\"text_wrapper\":null}]}");
      List<Layout> layouts = server.client().layouts().list();

      assertEquals("/api/v2/server/layouts", server.takeRequest().path());
      assertEquals("default", layouts.get(0).permalink());
      // A layout created with only an HTML wrapper comes back with an
      // explicit null for the text one.
      assertNull(layouts.get(0).textWrapper());
    }
  }

  @Test
  void layoutsCreateSendsTheWrapper() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201,
          "{\"status\":\"success\",\"time\":0.001,\"data\":{\"layout\":"
              + "{\"id\":1,\"name\":\"Default\",\"permalink\":\"default\"}}}");
      Layout layout =
          server
              .client()
              .layouts()
              .create(
                  LayoutRequest.builder()
                      .name("Default")
                      .permalink("default")
                      .htmlWrapper("<html>{{{ content }}}</html>")
                      .build());

      assertTrue(server.takeRequest().body().contains("html_wrapper"));
      assertEquals("Default", layout.name());
    }
  }

  @Test
  void layoutsCreateWithoutThePlaceholderIsRefused() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          422,
          "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\"ValidationError\","
              + "\"message\":\"html_wrapper must contain {{{ content }}}\"}}");

      CamelMailerException error =
          assertThrows(
              CamelMailerException.class,
              () ->
                  server
                      .client()
                      .layouts()
                      .create(
                          LayoutRequest.builder()
                              .name("Broken")
                              .htmlWrapper("<html></html>")
                              .build()));
      assertEquals("ValidationError", error.getCode());
    }
  }

  @Test
  void layoutsDeleteAndUploadLogo() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"deleted\":true}");
      assertTrue(server.client().layouts().delete("default"));
      assertEquals("/api/v2/server/layouts/default", server.takeRequest().path());

      server.enqueueData("{\"url\":\"https://app.camelmailer.com/assets/layouts/l-1/logo\"}");
      String url =
          server.client().layouts().uploadLogo("default", "data:image/png;base64,iVBORw0KGgo=");

      MockServer.Recorded recorded = server.takeRequest();
      assertTrue(recorded.body().contains("\"data_url\""));
      // The endpoint answers with "url", not "logo_url".
      assertEquals("https://app.camelmailer.com/assets/layouts/l-1/logo", url);
    }
  }
}
