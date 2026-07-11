package com.camelmailer.emails;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/**
 * Send and read emails: {@code /api/v2/server/messages}.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#emails()}.
 */
public final class Emails {

  private static final String BASE = "/api/v2/server/messages";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Emails(ApiClient client) {
    this.client = client;
  }

  /**
   * Sends an email. One message is queued per recipient.
   *
   * @param request the send parameters
   * @return the queue result
   */
  public SendResult send(SendEmailRequest request) {
    return client.convert(client.post(BASE, request), SendResult.class);
  }

  /**
   * Sends a batch of emails in one call. The batch itself always succeeds; inspect each {@link
   * BatchItem} for per-message success or failure.
   *
   * @param requests the send requests
   * @return one result per request, in order
   */
  public List<BatchItem> sendBatch(List<SendEmailRequest> requests) {
    JsonNode data = client.post(BASE + "/batch", requests);
    return client.convertList(data.path("messages"), BatchItem.class);
  }

  /**
   * Sends an email rendered from a stored template. The request must have {@link
   * SendEmailRequest.Builder#template template} set; fields set directly (e.g. subject) override
   * the rendered ones.
   *
   * @param request the send parameters including template and template model
   * @return the queue result
   */
  public SendResult sendWithTemplate(SendEmailRequest request) {
    return client.convert(client.post(BASE + "/with_template", request), SendResult.class);
  }

  /**
   * Sends a stored template to many recipients in one call.
   *
   * @param requests the send requests, each with a template set
   * @return one result per request, in order
   */
  public List<BatchItem> sendWithTemplateBatch(List<SendEmailRequest> requests) {
    JsonNode data = client.post(BASE + "/with_template/batch", requests);
    return client.convertList(data.path("messages"), BatchItem.class);
  }

  /**
   * Fetches a message with its delivery attempts.
   *
   * @param id the message id
   * @return message and deliveries
   */
  public EmailDetails get(long id) {
    return client.convert(client.get(BASE + "/" + id), EmailDetails.class);
  }

  /**
   * Lists messages, newest first.
   *
   * @return the first page of messages
   */
  public EmailList list() {
    return list(ListEmailsOptions.builder().build());
  }

  /**
   * Lists messages with filters and pagination.
   *
   * @param options filters and pagination
   * @return a page of messages
   */
  public EmailList list(ListEmailsOptions options) {
    return client.convert(client.get(BASE, options.toQuery()), EmailList.class);
  }

  /**
   * Lists the delivery attempts of a message.
   *
   * @param id the message id
   * @return delivery attempts
   */
  public List<Delivery> deliveries(long id) {
    JsonNode data = client.get(BASE + "/" + id + "/deliveries");
    return client.convertList(data.path("deliveries"), Delivery.class);
  }

  /**
   * Lists the open events of a message.
   *
   * @param id the message id
   * @return open events
   */
  public List<ActivityEvent> opens(long id) {
    JsonNode data = client.get(BASE + "/" + id + "/opens");
    return client.convertList(data.path("opens"), ActivityEvent.class);
  }

  /**
   * Lists the click events of a message.
   *
   * @param id the message id
   * @return click events
   */
  public List<ActivityEvent> clicks(long id) {
    JsonNode data = client.get(BASE + "/" + id + "/clicks");
    return client.convertList(data.path("clicks"), ActivityEvent.class);
  }

  /**
   * Fetches the raw RFC 5322 source of a message.
   *
   * @param id the message id
   * @return the raw message source
   */
  public String raw(long id) {
    return client.get(BASE + "/" + id + "/raw").path("raw_message").asText();
  }

  /**
   * Convenience overload of {@link #sendBatch(List)}.
   *
   * @param requests the send requests
   * @return one result per request, in order
   */
  public List<BatchItem> sendBatch(SendEmailRequest... requests) {
    return sendBatch(List.of(requests));
  }

  /**
   * Convenience for rendering-free template sends: builds the request from the template name and
   * model.
   *
   * @param template template permalink
   * @param model template variables
   * @param request base send parameters (from, to, ...)
   * @return the queue result
   */
  public SendResult sendWithTemplate(
      String template, Map<String, Object> model, SendEmailRequest.Builder request) {
    return sendWithTemplate(request.template(template).templateModel(model).build());
  }
}
