package com.camelmailer.templates;

/**
 * A template preview rendered against a model.
 *
 * @param subject rendered subject, may be {@code null}
 * @param htmlBody rendered HTML body, may be {@code null}
 * @param textBody rendered text body, may be {@code null}
 */
public record RenderedTemplate(String subject, String htmlBody, String textBody) {}
