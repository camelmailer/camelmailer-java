package com.camelmailer.layouts;

/**
 * A template layout: the wrapper shared by every template that uses it, so header, footer and
 * styling live in one place.
 *
 * @param id numeric layout id
 * @param uuid stable UUID
 * @param name display name
 * @param permalink URL-safe identifier used in API paths and by templates
 * @param htmlWrapper wrapper for the HTML body; embeds it with <code>{{{ content }}}</code>
 * @param textWrapper wrapper for the plain-text body, or {@code null} when the layout has none
 */
public record Layout(
    long id, String uuid, String name, String permalink, String htmlWrapper, String textWrapper) {}
