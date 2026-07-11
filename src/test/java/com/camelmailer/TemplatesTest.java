package com.camelmailer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camelmailer.templates.RenderedTemplate;
import com.camelmailer.templates.Template;
import com.camelmailer.templates.TemplateRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplatesTest {

  private static final String TEMPLATE =
      "{\"id\":3,\"uuid\":\"u-3\",\"name\":\"Welcome\",\"permalink\":\"welcome\","
          + "\"subject\":\"Hi {{ name }}\",\"html_body\":\"<p>Hi {{ name }}</p>\","
          + "\"text_body\":\"Hi {{ name }}\",\"archived\":false}";

  @Test
  void listParsesTemplates() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"templates\":[" + TEMPLATE + "]}");
      List<Template> templates = server.client().templates().list();

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("GET", recorded.method());
      assertEquals("/api/v2/server/templates", recorded.path());
      assertEquals(1, templates.size());
      assertEquals("welcome", templates.get(0).permalink());
      assertEquals("Hi {{ name }}", templates.get(0).subject());
    }
  }

  @Test
  void createPostsSnakeCaseFields() {
    try (MockServer server = new MockServer()) {
      server.enqueue(
          201, "{\"status\":\"success\",\"time\":0.001,\"data\":{\"template\":" + TEMPLATE + "}}");
      Template template =
          server
              .client()
              .templates()
              .create(
                  TemplateRequest.builder()
                      .name("Welcome")
                      .subject("Hi {{ name }}")
                      .htmlBody("<p>Hi {{ name }}</p>")
                      .textBody("Hi {{ name }}")
                      .build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/templates", recorded.path());
      assertTrue(recorded.body().contains("\"name\":\"Welcome\""));
      assertTrue(recorded.body().contains("\"html_body\":\"<p>Hi {{ name }}</p>\""));
      assertTrue(recorded.body().contains("\"text_body\":\"Hi {{ name }}\""));
      assertEquals(3L, template.id());
      assertEquals("u-3", template.uuid());
    }
  }

  @Test
  void getFetchesByPermalink() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"template\":" + TEMPLATE + "}");
      Template template = server.client().templates().get("welcome");
      assertEquals("/api/v2/server/templates/welcome", server.takeRequest().path());
      assertEquals("Welcome", template.name());
    }
  }

  @Test
  void updatePatchesOnlySetFields() {
    try (MockServer server = new MockServer()) {
      server.enqueueData("{\"template\":" + TEMPLATE + "}");
      server
          .client()
          .templates()
          .update("welcome", TemplateRequest.builder().subject("New subject").build());

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("PATCH", recorded.method());
      assertEquals("/api/v2/server/templates/welcome", recorded.path());
      assertTrue(recorded.body().contains("\"subject\":\"New subject\""));
      assertFalse(recorded.body().contains("\"name\""), "unset fields must be omitted");
      assertFalse(recorded.body().contains("\"html_body\""), "unset fields must be omitted");
    }
  }

  @Test
  void archivePostsToArchive() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"template\":{\"id\":3,\"uuid\":\"u-3\",\"name\":\"Welcome\","
              + "\"permalink\":\"welcome\",\"archived\":true}}");
      Template template = server.client().templates().archive("welcome");

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/templates/welcome/archive", recorded.path());
      assertTrue(template.archived());
    }
  }

  @Test
  void renderPostsModelAndParsesRenderedFields() {
    try (MockServer server = new MockServer()) {
      server.enqueueData(
          "{\"rendered\":{\"subject\":\"Hi Ada\",\"html_body\":\"<p>Hi Ada</p>\","
              + "\"text_body\":\"Hi Ada\"}}");
      RenderedTemplate rendered =
          server.client().templates().render("welcome", Map.of("name", "Ada"));

      MockServer.Recorded recorded = server.takeRequest();
      assertEquals("POST", recorded.method());
      assertEquals("/api/v2/server/templates/welcome/render", recorded.path());
      assertTrue(recorded.body().contains("\"template_model\":{\"name\":\"Ada\"}"));
      assertEquals("Hi Ada", rendered.subject());
      assertEquals("<p>Hi Ada</p>", rendered.htmlBody());
      assertEquals("Hi Ada", rendered.textBody());
    }
  }

  @Test
  void notFoundTemplateThrowsTypedException() {
    try (MockServer server = new MockServer()) {
      server.enqueueError(404, "NotFound", "template not found");
      CamelMailerException e =
          assertThrows(CamelMailerException.class, () -> server.client().templates().get("nope"));
      assertEquals("NotFound", e.getCode());
      assertEquals(404, e.getStatusCode());
    }
  }
}
