package com.camelmailer.emails;

/**
 * An engagement event (open or click) of a message.
 *
 * @param ipAddress client IP address, may be {@code null}
 * @param userAgent client user agent, may be {@code null}
 * @param url clicked URL (clicks only), may be {@code null}
 * @param createdAt event timestamp (RFC 3339)
 */
public record ActivityEvent(String ipAddress, String userAgent, String url, String createdAt) {}
