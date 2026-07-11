package com.camelmailer.emails;

import java.util.Map;

/**
 * A stored message (outgoing or incoming), as returned by the messages and bounces endpoints.
 *
 * @param id numeric message id
 * @param token public message token
 * @param scope {@code incoming} or {@code outgoing}
 * @param rcptTo recipient address
 * @param mailFrom sender address, may be {@code null}
 * @param subject subject line, may be {@code null}
 * @param messageId the {@code Message-ID} header value, may be {@code null}
 * @param tag free-form tag, may be {@code null}
 * @param status delivery status (e.g. {@code Sent}), may be {@code null}
 * @param bounce whether this message is a bounce
 * @param spamStatus spam verdict, may be {@code null}
 * @param spamScore spam score, may be {@code null}
 * @param held whether the message is held
 * @param threat whether the message was flagged as a threat
 * @param size message size in bytes, may be {@code null}
 * @param metadata metadata supplied at send time, may be {@code null}
 * @param streamId id of the message stream, may be {@code null}
 * @param bypassed whether a hold was bypassed
 * @param createdAt creation timestamp (RFC 3339)
 */
public record Email(
    long id,
    String token,
    String scope,
    String rcptTo,
    String mailFrom,
    String subject,
    String messageId,
    String tag,
    String status,
    boolean bounce,
    String spamStatus,
    Double spamScore,
    boolean held,
    boolean threat,
    Long size,
    Map<String, Object> metadata,
    Long streamId,
    boolean bypassed,
    String createdAt) {}
