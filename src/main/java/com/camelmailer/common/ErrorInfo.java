package com.camelmailer.common;

/**
 * A structured API error, as embedded in batch results.
 *
 * @param code stable error code, e.g. {@code ValidationError}
 * @param message human-readable error message
 */
public record ErrorInfo(String code, String message) {}
