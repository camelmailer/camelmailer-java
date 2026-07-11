package com.camelmailer.dmarc;

import com.camelmailer.common.Pagination;
import java.util.List;

/**
 * A page of DMARC aggregate reports.
 *
 * @param reports the reports on this page, newest report range first
 * @param pagination pagination metadata
 */
public record DmarcReportList(List<DmarcReport> reports, Pagination pagination) {}
