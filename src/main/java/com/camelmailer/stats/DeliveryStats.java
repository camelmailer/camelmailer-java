package com.camelmailer.stats;

import java.util.List;

/**
 * Pending outbound queue depth, in total and per recipient domain.
 *
 * @param queued messages currently queued
 * @param domains per-domain queue depth
 */
public record DeliveryStats(long queued, List<DomainQueue> domains) {

  /**
   * Queue depth for one recipient domain.
   *
   * @param domain the recipient domain
   * @param queued messages queued for that domain
   */
  public record DomainQueue(String domain, long queued) {}
}
