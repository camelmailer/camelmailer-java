package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.emails.SendEmailRequest;
import com.camelmailer.emails.StreamSendResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdempotencyTest {

  private static SendEmailRequest receipt() {
    return SendEmailRequest.builder()
        .from("billing@acme.com")
        .to("ada@example.com")
        .subject("Your receipt")
        .textBody("Thanks.")
        .build();
  }

  @Test
  void theKeyTravelsAsAHeaderNotInTheBody() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"message_id\":1,\"recipients\":[]}");
      server.client().emails().send(receipt(), "order-4711");

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("order-4711", recorded.headers().getFirst("Idempotency-Key"));
      // The body is what the server hashes for the claim, so the key must
      // not end up inside it.
      assertFalse(recorded.body().contains("idempotency"));
      assertFalse(recorded.body().contains("Idempotency"));
    }
  }

  @Test
  void noHeaderWithoutAKey() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"message_id\":1}");
      server.client().emails().send(receipt());
      assertNull(server.takeRequest().headers().getFirst("Idempotency-Key"));
    }
  }

  @Test
  void everySendEndpointCarriesTheKey() {
    // The API claims all four, so all four have to send it.
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"message_id\":1}");
      server.client().emails().send(receipt(), "k");
      assertEquals("k", server.takeRequest().headers().getFirst("Idempotency-Key"));

      server.enqueueData("{\"messages\":[]}");
      server.client().emails().sendBatch(List.of(receipt()), "k");
      MockServer.Recorded batch = server.takeRequest();
      assertEquals("/api/v2/server/messages/batch", batch.path());
      assertEquals("k", batch.headers().getFirst("Idempotency-Key"));

      server.enqueueData("{\"message_id\":1}");
      server.client().emails().sendWithTemplate(receipt(), "k");
      MockServer.Recorded template = server.takeRequest();
      assertEquals("/api/v2/server/messages/with_template", template.path());
      assertEquals("k", template.headers().getFirst("Idempotency-Key"));

      server.enqueueData("{\"messages\":[]}");
      server.client().emails().sendWithTemplateBatch(List.of(receipt()), "k");
      MockServer.Recorded templateBatch = server.takeRequest();
      assertEquals("/api/v2/server/messages/with_template/batch", templateBatch.path());
      assertEquals("k", templateBatch.headers().getFirst("Idempotency-Key"));
    }
  }

  @Test
  void aReusedKeyForAnotherBodyIsRefused() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          409,
          "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\"InvalidIdempotentRequest\","
              + "\"message\":\"The same idempotency key was used with a different request\"}}");

      CamelMailerException error =
          assertThrows(
              CamelMailerException.class, () -> server.client().emails().send(receipt(), "reused"));
      assertEquals("InvalidIdempotentRequest", error.getCode());
      assertEquals(409, error.getStatusCode());
    }
  }

  @Test
  void theSendAllowanceSurfacesAsSendLimitExceeded() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          429,
          "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\"SendLimitExceeded\","
              + "\"message\":\"the send allowance is used up\"}}");

      CamelMailerException error =
          assertThrows(CamelMailerException.class, () -> server.client().emails().send(receipt()));
      assertEquals("SendLimitExceeded", error.getCode());
      assertEquals(429, error.getStatusCode());
    }
  }

  @Test
  void sendToStreamCountsQueuedAgainstSkipped() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"queued\":42,\"skipped\":3}");
      StreamSendResult result =
          server
              .client()
              .emails()
              .sendToStream(
                  "newsletter",
                  SendEmailRequest.builder()
                      .from("news@acme.com")
                      .subject("September")
                      .textBody("Hello.")
                      .build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("/api/v2/server/streams/newsletter/send", recorded.path());
      assertTrue(recorded.body().contains("\"from\""));
      assertEquals(42L, result.queued());
      assertEquals(3L, result.skipped());
    }
  }
}
