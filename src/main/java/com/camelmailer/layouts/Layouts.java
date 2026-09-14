package com.camelmailer.layouts;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 * Template layouts: {@code /api/v2/server/layouts}.
 *
 * <p>A layout wraps every template that uses it, so header, footer and styling live in one place
 * instead of in each template.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#layouts()}.
 */
public final class Layouts {

  private static final String BASE = "/api/v2/server/layouts";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Layouts(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists all layouts of the server.
   *
   * @return the layouts
   */
  public List<Layout> list() {
    return client.convertList(client.get(BASE).path("layouts"), Layout.class);
  }

  /**
   * Creates a layout.
   *
   * <p>{@code htmlWrapper} has to embed the body with <code>{{{ content }}}</code>; anything else
   * is refused with {@code ValidationError}.
   *
   * @param request the layout fields
   * @return the created layout
   */
  public Layout create(LayoutRequest request) {
    return client.convert(client.post(BASE, request).path("layout"), Layout.class);
  }

  /**
   * Fetches a layout by permalink.
   *
   * @param permalink the layout permalink
   * @return the layout
   */
  public Layout get(String permalink) {
    return client.convert(client.get(BASE + "/" + permalink).path("layout"), Layout.class);
  }

  /**
   * Updates a layout; only the given fields change.
   *
   * @param permalink the layout permalink
   * @param request the fields to change
   * @return the updated layout
   */
  public Layout update(String permalink, LayoutRequest request) {
    return client.convert(
        client.patch(BASE + "/" + permalink, request).path("layout"), Layout.class);
  }

  /**
   * Deletes a layout. Templates that referenced it fall back to no wrapper.
   *
   * @param permalink the layout permalink
   * @return whether the layout was removed
   */
  public boolean delete(String permalink) {
    return client.delete(BASE + "/" + permalink).path("deleted").asBoolean();
  }

  /**
   * Uploads the layout's logo, given as a {@code data:image/png;base64,...} URL.
   *
   * @param permalink the layout permalink
   * @param dataUrl the logo as a data URL
   * @return the absolute URL to reference from the wrapper; it is served without authentication,
   *     because mail clients fetch it without a session
   */
  public String uploadLogo(String permalink, String dataUrl) {
    ObjectNode body = client.newObject();
    body.put("data_url", dataUrl);
    // The endpoint answers with "url", not "logo_url".
    return client.post(BASE + "/" + permalink + "/logo", body).path("url").asText();
  }
}
