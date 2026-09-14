package com.camelmailer.campaigns;

/**
 * The audience stream carried on every campaign, so a list needs no second lookup.
 *
 * @param permalink URL-safe identifier of the stream
 * @param name display name of the stream
 */
public record CampaignStream(String permalink, String name) {}
