package com.camelmailer.inbound;

import com.camelmailer.emails.Email;

/**
 * What a retry or bypass did.
 *
 * @param requeued whether the message went back on the delivery queue; the API names this field
 *     {@code requeued}
 * @param message the message as it now stands
 */
public record RequeueResult(boolean requeued, Email message) {}
