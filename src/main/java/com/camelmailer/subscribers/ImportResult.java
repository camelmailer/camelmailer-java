package com.camelmailer.subscribers;

/**
 * What an import wrote.
 *
 * <p>Blanks and duplicates within the request are skipped, so {@code added} can be lower than the
 * number of addresses passed.
 *
 * @param added subscriptions written
 * @param total the stream's subscriber count after the import
 */
public record ImportResult(long added, long total) {}
