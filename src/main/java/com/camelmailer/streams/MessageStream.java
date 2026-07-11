package com.camelmailer.streams;

/**
 * A message stream — a named channel messages are sent through.
 *
 * @param id numeric stream id
 * @param uuid stream UUID
 * @param name display name
 * @param permalink URL-safe identifier used in API paths and sends
 * @param streamType {@code transactional} or {@code broadcast}
 * @param archived whether the stream is archived
 */
public record MessageStream(
    long id, String uuid, String name, String permalink, String streamType, boolean archived) {}
