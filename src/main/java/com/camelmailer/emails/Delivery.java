package com.camelmailer.emails;

/**
 * One delivery attempt of a message.
 *
 * @param id delivery id
 * @param status delivery status, e.g. {@code Sent}
 * @param details human-readable details, may be {@code null}
 * @param output SMTP transcript output, may be {@code null}
 * @param sentWithSsl whether the delivery used TLS, may be {@code null}
 * @param createdAt attempt timestamp (RFC 3339)
 */
public record Delivery(
    long id, String status, String details, String output, Boolean sentWithSsl, String createdAt) {}
