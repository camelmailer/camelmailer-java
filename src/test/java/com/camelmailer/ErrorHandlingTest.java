package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ErrorHandlingTest {

  @Test
  void unauthorizedIsMappedToTypedException() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(401, "Unauthorized", "invalid API key");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().stats().get());
      assertEquals("Unauthorized", e.getCode());
      assertEquals("invalid API key", e.getMessage());
      assertEquals(401, e.getStatusCode());
    }
  }

  @Test
  void notFoundIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(404, "NotFound", "message not found");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().emails().get(42));
      assertEquals("NotFound", e.getCode());
      assertEquals(404, e.getStatusCode());
    }
  }

  @Test
  void validationErrorIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(422, "ValidationError", "from is not a verified domain");
      CamelMailerException e =
          assertThrows(
              CamelMailerException.class,
              () ->
                  server
                      .client()
                      .emails()
                      .send(
                          com.camelmailer.emails.SendEmailRequest.builder()
                              .from("a@b.c")
                              .to("d@e.f")
                              .build()));
      assertEquals("ValidationError", e.getCode());
      assertEquals(422, e.getStatusCode());
    }
  }

  @Test
  void parameterMissingIsMapped() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(400, "ParameterMissing", "param is missing: template");
      CamelMailerException e =
          assertThrows(
              CamelMailerException.class,
              () ->
                  server
                      .client()
                      .emails()
                      .sendWithTemplate(
                          com.camelmailer.emails.SendEmailRequest.builder().from("a@b.c").build()));
      assertEquals("ParameterMissing", e.getCode());
      assertEquals(400, e.getStatusCode());
    }
  }

  @Test
  void errorEnvelopeWithHttp200IsStillAnError() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          200,
          "{\"status\":\"error\",\"time\":0.001,\"error\":{\"code\":\"BillingDisabled\",\"message\":\"upgrade required\"}}");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().stats().get());
      assertEquals("BillingDisabled", e.getCode());
      assertEquals(200, e.getStatusCode());
    }
  }

  @Test
  void errorWithoutCodeFallsBackToHttpError() {
    try (MockServer server = new MockServer()) {
      server.enqueue(500, "{\"status\":\"error\",\"time\":0.001,\"error\":{}}");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().stats().get());
      assertEquals("HttpError", e.getCode());
      assertEquals(500, e.getStatusCode());
    }
  }

  @Test
  void nonJsonBodyBecomesInvalidResponse() {
    try (MockServer server = new MockServer()) {
      server.enqueue(502, "<html>bad gateway</html>");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().stats().get());
      assertEquals("InvalidResponse", e.getCode());
      assertEquals(502, e.getStatusCode());
    }
  }

  @Test
  void connectionFailureBecomesConnectionError() {
    MockServer server = new MockServer();
    String baseUrl = server.baseUrl();
    server.close();
    CamelMailer client = CamelMailer.builder().apiKey("cm_test_key").baseUrl(baseUrl).build();
    CamelMailerException e = assertThrows(CamelMailerException.class, () -> client.stats().get());
    assertEquals("ConnectionError", e.getCode());
    assertEquals(0, e.getStatusCode());
  }
}
