package com.camelmailer.campaigns;

/**
 * A campaign together with its statistics.
 *
 * @param campaign the campaign itself
 * @param stats its counters
 */
public record CampaignDetail(Campaign campaign, CampaignStats stats) {}
