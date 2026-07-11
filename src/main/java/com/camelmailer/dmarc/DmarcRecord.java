package com.camelmailer.dmarc;

/**
 * One row of a DMARC aggregate report.
 *
 * @param id numeric record id
 * @param sourceIp the sending IP address
 * @param count number of messages this row covers
 * @param disposition applied policy: {@code none}, {@code quarantine}, or {@code reject}
 * @param dkimResult raw DKIM result, may be {@code null}
 * @param spfResult raw SPF result, may be {@code null}
 * @param dkimAligned whether DKIM was aligned
 * @param spfAligned whether SPF was aligned
 * @param headerFrom the {@code From} header domain, may be {@code null}
 * @param envelopeFrom the envelope sender domain, may be {@code null}
 */
public record DmarcRecord(
    long id,
    String sourceIp,
    long count,
    String disposition,
    String dkimResult,
    String spfResult,
    boolean dkimAligned,
    boolean spfAligned,
    String headerFrom,
    String envelopeFrom) {}
