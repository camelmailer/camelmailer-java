package com.camelmailer.campaigns;

import com.camelmailer.http.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 * Broadcast campaigns: {@code /api/v2/server/campaigns}.
 *
 * <p>A campaign is content plus an audience. There are two ways to create one and they behave
 * differently: {@link #createDraft} writes it and waits, while {@link #createAndSend} expands it to
 * the stream's subscribers before the call returns.
 *
 * <p>Obtain via {@link com.camelmailer.CamelMailer#campaigns()}.
 */
public final class Campaigns {

  private static final String BASE = "/api/v2/server/campaigns";
  private static final String STREAMS = "/api/v2/server/streams";

  private final ApiClient client;

  /**
   * Creates the service.
   *
   * @param client the internal API client
   */
  public Campaigns(ApiClient client) {
    this.client = client;
  }

  /**
   * Lists every campaign of the server, newest first.
   *
   * @return the campaigns
   */
  public List<Campaign> list() {
    return client.convertList(client.get(BASE).path("campaigns"), Campaign.class);
  }

  /**
   * Lists the campaigns of one broadcast stream.
   *
   * @param permalink the stream permalink
   * @return the campaigns of that stream
   */
  public List<Campaign> listForStream(String permalink) {
    JsonNode data = client.get(STREAMS + "/" + permalink + "/campaigns");
    return client.convertList(data.path("campaigns"), Campaign.class);
  }

  /**
   * Fetches a campaign together with its statistics.
   *
   * @param id the campaign id
   * @return the campaign and its counters
   */
  public CampaignDetail get(long id) {
    return client.convert(client.get(BASE + "/" + id), CampaignDetail.class);
  }

  /**
   * Fetches a campaign through its stream.
   *
   * @param permalink the stream permalink
   * @param id the campaign id
   * @return the campaign and its counters
   */
  public CampaignDetail getForStream(String permalink, long id) {
    return client.convert(
        client.get(STREAMS + "/" + permalink + "/campaigns/" + id), CampaignDetail.class);
  }

  /**
   * Creates a campaign without sending it.
   *
   * <p>Name the audience with {@code stream}. Leave the schedule unset for a draft, set it for a
   * scheduled send, or set {@code sendNow} to send on creation.
   *
   * @param request the campaign fields
   * @return the created campaign
   */
  public Campaign createDraft(DraftCampaignRequest request) {
    return client.convert(client.post(BASE, request).path("campaign"), Campaign.class);
  }

  /**
   * Creates a campaign on a broadcast stream and sends it immediately.
   *
   * <p>The send starts before this call returns, so there is no draft to review and no schedule to
   * set. Use {@link #createDraft} when the campaign should wait.
   *
   * @param permalink the broadcast stream permalink
   * @param request the campaign fields
   * @return the created campaign, already sending
   */
  public Campaign createAndSend(String permalink, SendCampaignRequest request) {
    JsonNode data = client.post(STREAMS + "/" + permalink + "/campaigns", request);
    return client.convert(data.path("campaign"), Campaign.class);
  }

  /**
   * Updates a draft or scheduled campaign.
   *
   * <p>A campaign that is already sending cannot be edited and the API answers {@code
   * ValidationError}.
   *
   * @param id the campaign id
   * @param request the fields to change
   * @return the updated campaign
   */
  public Campaign update(long id, UpdateCampaignRequest request) {
    ObjectNode body = client.newObject();
    putIfSet(body, "name", request.getName());
    putIfSet(body, "from", request.getFrom());
    putIfSet(body, "subject", request.getSubject());
    putIfSet(body, "html_body", request.getHtmlBody());
    putIfSet(body, "text_body", request.getTextBody());
    if (request.isClearSchedule()) {
      // An omitted field leaves the schedule standing; only an explicit
      // null drops the campaign back to a draft.
      body.putNull("scheduled_at");
    } else {
      putIfSet(body, "scheduled_at", request.getScheduledAt());
    }
    return client.convert(client.patch(BASE + "/" + id, body).path("campaign"), Campaign.class);
  }

  /**
   * Sends a campaign now, whatever its schedule said.
   *
   * @param id the campaign id
   * @return the campaign, now sending
   */
  public Campaign send(long id) {
    return client.convert(
        client.post(BASE + "/" + id + "/send", null).path("campaign"), Campaign.class);
  }

  /**
   * Cancels a scheduled or in-flight campaign. Messages already queued are not recalled.
   *
   * @param id the campaign id
   * @return the canceled campaign
   */
  public Campaign cancel(long id) {
    return client.convert(
        client.post(BASE + "/" + id + "/cancel", null).path("campaign"), Campaign.class);
  }

  private static void putIfSet(ObjectNode body, String field, String value) {
    if (value != null) {
      body.put(field, value);
    }
  }
}
