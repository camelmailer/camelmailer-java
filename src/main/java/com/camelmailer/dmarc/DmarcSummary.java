package com.camelmailer.dmarc;

import java.util.List;
import java.util.Map;

/**
 * DMARC compliance summary over the stored aggregate-report rows.
 *
 * @param total messages covered by the reports
 * @param pass messages with DKIM and SPF aligned
 * @param fail messages failing alignment
 * @param passRate {@code pass / total} (0.0–1.0)
 * @param bySource top 20 sending sources by volume
 * @param byDisposition message counts per disposition ({@code none}, {@code quarantine}, {@code
 *     reject})
 */
public record DmarcSummary(
    long total,
    long pass,
    long fail,
    double passRate,
    List<Source> bySource,
    Map<String, Long> byDisposition) {

  /**
   * Alignment statistics for one sending source.
   *
   * @param sourceIp the sending IP address
   * @param count messages from this source
   * @param spfAlignedPct percentage of messages with SPF aligned
   * @param dkimAlignedPct percentage of messages with DKIM aligned
   * @param dispositionCounts message counts per disposition
   */
  public record Source(
      String sourceIp,
      long count,
      double spfAlignedPct,
      double dkimAlignedPct,
      Map<String, Long> dispositionCounts) {}
}
