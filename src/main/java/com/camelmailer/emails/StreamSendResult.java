package com.camelmailer.emails;

/**
 * How a broadcast to a stream was split.
 *
 * @param queued recipients queued
 * @param skipped recipients past the per-request cap of 1000
 */
public record StreamSendResult(long queued, long skipped) {}
