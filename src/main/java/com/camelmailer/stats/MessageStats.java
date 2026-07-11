package com.camelmailer.stats;

/**
 * Aggregate message and engagement counters of a server.
 *
 * @param total total number of messages
 * @param incoming incoming messages
 * @param outgoing outgoing messages
 * @param sent successfully sent messages
 * @param pending messages waiting in the queue
 * @param held held messages
 * @param bounced bounced messages
 * @param softFail messages with soft failures
 * @param hardFail messages with hard failures
 * @param opens total open events
 * @param clicks total click events
 * @param uniqueOpens messages opened at least once
 * @param uniqueClicks messages clicked at least once
 */
public record MessageStats(
    long total,
    long incoming,
    long outgoing,
    long sent,
    long pending,
    long held,
    long bounced,
    long softFail,
    long hardFail,
    long opens,
    long clicks,
    long uniqueOpens,
    long uniqueClicks) {}
