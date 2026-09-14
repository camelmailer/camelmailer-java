package com.camelmailer.subscribers;

/**
 * One address on a broadcast stream.
 *
 * @param id numeric subscriber id
 * @param address the email address
 * @param status {@code subscribed} or {@code unsubscribed}
 * @param createdAt when the subscription row was created
 */
public record Subscriber(long id, String address, String status, String createdAt) {}
