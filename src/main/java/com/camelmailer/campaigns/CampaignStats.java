package com.camelmailer.campaigns;

/**
 * Per-campaign counters, attributed through the messages the campaign produced.
 *
 * @param total recipient count of the campaign
 * @param sent messages created
 * @param delivered messages delivered
 * @param failed messages that failed
 * @param opened messages opened at least once
 * @param clicked messages with at least one click
 * @param unsubscribed resulting unsubscribes
 */
public record CampaignStats(
    long total,
    long sent,
    long delivered,
    long failed,
    long opened,
    long clicked,
    long unsubscribed) {}
