package com.camelmailer.campaigns;

/**
 * A broadcast campaign: content plus an audience.
 *
 * @param id numeric campaign id
 * @param name display name
 * @param subject message subject
 * @param from sender address
 * @param htmlBody HTML part
 * @param textBody plain-text part
 * @param status lifecycle state: {@code draft}, {@code scheduled}, {@code sending}, {@code sent},
 *     {@code failed} or {@code canceled}. Only draft and scheduled are editable.
 * @param total recipient count snapshotted when the send begins
 * @param sent recipients expanded into messages so far
 * @param streamId id of the audience stream
 * @param stream permalink and name of the audience stream
 * @param scheduledAt send time of a scheduled campaign, or {@code null}
 * @param createdAt when the campaign row was created
 * @param completedAt when expansion finished ({@code sent} or {@code failed})
 */
public record Campaign(
    long id,
    String name,
    String subject,
    String from,
    String htmlBody,
    String textBody,
    String status,
    long total,
    long sent,
    long streamId,
    CampaignStream stream,
    String scheduledAt,
    String createdAt,
    String completedAt) {}
