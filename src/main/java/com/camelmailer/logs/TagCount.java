package com.camelmailer.logs;

/**
 * One tag with how often the server's recent messages used it.
 *
 * @param tag the tag itself
 * @param count how many messages carry it
 */
public record TagCount(String tag, long count) {}
