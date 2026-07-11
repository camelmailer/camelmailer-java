package com.camelmailer.dmarc;

import java.util.List;

/**
 * A DMARC report with its individual records.
 *
 * @param report the report
 * @param records the rows of the report
 */
public record DmarcReportDetail(DmarcReport report, List<DmarcRecord> records) {}
