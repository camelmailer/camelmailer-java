package com.camelmailer.templates;

/**
 * A stored message template. Subject and bodies may contain Mustache-style {@code {{ variables }}}.
 *
 * @param id numeric template id
 * @param uuid template UUID
 * @param name display name
 * @param permalink URL-safe identifier used in API paths and sends
 * @param subject subject template, may be {@code null}
 * @param htmlBody HTML body template, may be {@code null}
 * @param textBody text body template, may be {@code null}
 * @param archived whether the template is archived
 */
public record Template(
    long id,
    String uuid,
    String name,
    String permalink,
    String subject,
    String htmlBody,
    String textBody,
    boolean archived) {}
