package com.camelmailer.dmarc;

/**
 * A stored DMARC aggregate report.
 *
 * @param id numeric report id
 * @param domain the reported domain
 * @param orgName reporting organization, may be {@code null}
 * @param orgEmail reporting organization contact, may be {@code null}
 * @param reportId the reporter's report id
 * @param dateRangeBegin start of the covered range (RFC 3339)
 * @param dateRangeEnd end of the covered range (RFC 3339)
 * @param receivedAt when the report was ingested (RFC 3339)
 * @param recordCount number of records in the report
 */
public record DmarcReport(
    long id,
    String domain,
    String orgName,
    String orgEmail,
    String reportId,
    String dateRangeBegin,
    String dateRangeEnd,
    String receivedAt,
    long recordCount) {}
