package com.camelmailer.emails;

import java.util.List;

/**
 * Result of queuing a message: the shared message id and one entry per recipient.
 *
 * @param messageId id shared by all queued copies, or {@code null} when nothing was queued
 * @param recipients per-recipient queue results
 */
public record SendResult(Long messageId, List<Recipient> recipients) {

  /**
   * Queue result for a single recipient.
   *
   * @param messageId id of the queued message copy
   * @param rcptTo the recipient address
   * @param status queue status, e.g. {@code queued}
   * @param token public token of the queued message
   */
  public record Recipient(long messageId, String rcptTo, String status, String token) {}
}
