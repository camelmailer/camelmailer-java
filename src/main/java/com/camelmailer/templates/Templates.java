package com.camelmailer.templates;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/**
 * Manage message templates: {@code /api/v2/server/templates}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#templates()}.
 */
public final class Templates {

  private static final String BASE = "/api/v2/server/templates";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Templates(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists all templates.
   *
   * @return the templates
   */
  public List<Template> list() {
    JsonNode data = client.get(BASE);
    return client.convertList(data.path("templates"), Template.class);
  }

  /**
   * Creates a template.
   *
   * @param request template fields; name is required
   * @return the created template
   */
  public Template create(TemplateRequest request) {
    return client.convert(client.post(BASE, request).path("template"), Template.class);
  }

  /**
   * Fetches a template.
   *
   * @param permalink the template permalink
   * @return the template
   */
  public Template get(String permalink) {
    return client.convert(client.get(BASE + "/" + permalink).path("template"), Template.class);
  }

  /**
   * Updates a template.
   *
   * @param permalink the template permalink
   * @param request the fields to change
   * @return the updated template
   */
  public Template update(String permalink, TemplateRequest request) {
    return client.convert(
        client.patch(BASE + "/" + permalink, request).path("template"), Template.class);
  }

  /**
   * Archives a template.
   *
   * @param permalink the template permalink
   * @return the archived template
   */
  public Template archive(String permalink) {
    return client.convert(
        client.post(BASE + "/" + permalink + "/archive", null).path("template"), Template.class);
  }

  /**
   * Renders a template against a model without sending (preview).
   *
   * @param permalink the template permalink
   * @param templateModel template variables
   * @return the rendered subject and bodies
   */
  public RenderedTemplate render(String permalink, Map<String, Object> templateModel) {
    JsonNode data =
        client.post(BASE + "/" + permalink + "/render", Map.of("template_model", templateModel));
    return client.convert(data.path("rendered"), RenderedTemplate.class);
  }
}
