package com.camelmailer.inbound;

/**
 * What a retry or bypass did.
 *
 * @param queued whether the message went back on the delivery queue
 */
public record RequeueResult(boolean queued) {}
