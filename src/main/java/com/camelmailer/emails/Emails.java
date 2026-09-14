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
    return send(request, null);
  }

  /**
   * Sends an email with an idempotency key.
   *
   * <p>The same key with the same body returns the original result instead of queuing a second
   * copy; the same key with a different body is refused with {@code InvalidIdempotentRequest} (HTTP
   * 409) rather than silently ignored. Keys are scoped to the server and a completed result is kept
   * for 24 hours.
   *
   * @param request the send parameters
   * @param idempotencyKey the key, or {@code null} to send none
   * @return the queue result
   */
  public SendResult send(SendEmailRequest request, String idempotencyKey) {
    return client.convert(client.post(BASE, request, idempotencyKey), SendResult.class);
  }

  /**
   * Sends the same content to every subscriber of a broadcast stream.
   *
   * <p>The result counts what was queued against what was skipped: recipients past the per-request
   * cap of 1000 are skipped rather than queued, so a larger audience wants a campaign.
   *
   * @param permalink the broadcast stream permalink
   * @param request the content; give either a subject with a body, or a template
   * @return how the broadcast was split
   */
  public StreamSendResult sendToStream(String permalink, SendEmailRequest request) {
    return client.convert(
        client.post("/api/v2/server/streams/" + permalink + "/send", request),
        StreamSendResult.class);
  }

  /**
   * Sends a batch of emails in one call. The batch itself always succeeds; inspect each {@link
   * BatchItem} for per-message success or failure.
   *
   * @param requests the send requests
   * @return one result per request, in order
   */
  public List<BatchItem> sendBatch(List<SendEmailRequest> requests) {
    return sendBatch(requests, null);
  }

  /**
   * Sends a batch of emails with an idempotency key. See {@link #send(SendEmailRequest, String)}.
   *
   * @param requests the send requests
   * @param idempotencyKey the key, or {@code null} to send none
   * @return one result per request, in order
   */
  public List<BatchItem> sendBatch(List<SendEmailRequest> requests, String idempotencyKey) {
    JsonNode data = client.post(BASE + "/batch", requests, idempotencyKey);
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
    return sendWithTemplate(request, null);
  }

  /**
   * Sends a templated email with an idempotency key. See {@link #send(SendEmailRequest, String)}.
   *
   * @param request the send parameters including template and template model
   * @param idempotencyKey the key, or {@code null} to send none
   * @return the queue result
   */
  public SendResult sendWithTemplate(SendEmailRequest request, String idempotencyKey) {
    return client.convert(
        client.post(BASE + "/with_template", request, idempotencyKey), SendResult.class);
  }

  /**
   * Sends a stored template to many recipients in one call.
   *
   * @param requests the send requests, each with a template set
   * @return one result per request, in order
   */
  public List<BatchItem> sendWithTemplateBatch(List<SendEmailRequest> requests) {
    return sendWithTemplateBatch(requests, null);
  }

  /**
   * Sends a templated batch with an idempotency key. See {@link #send(SendEmailRequest, String)}.
   *
   * @param requests the send requests, each with a template set
   * @param idempotencyKey the key, or {@code null} to send none
   * @return one result per request, in order
   */
  public List<BatchItem> sendWithTemplateBatch(
      List<SendEmailRequest> requests, String idempotencyKey) {
    JsonNode data = client.post(BASE + "/with_template/batch", requests, idempotencyKey);
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
