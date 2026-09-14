package com.camelmailer.logs;

/**
 * One logged API request.
 *
 * @param id numeric log id
 * @param method the HTTP method
 * @param path the request path
 * @param statusCode the status the API answered with
 * @param durationMs how long the request took, in milliseconds
 * @param userAgent the client's User-Agent header
 * @param createdAt when the request arrived
 */
public record ApiRequestLog(
    long id,
    String method,
    String path,
    int statusCode,
    long durationMs,
    String userAgent,
    String createdAt) {}
