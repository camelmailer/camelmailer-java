package com.camelmailer.logs;

import com.camelmailer.common.Pagination;
import java.util.List;

/**
 * One page of logged requests.
 *
 * @param requests the page of logged requests
 * @param pagination the page window
 */
public record LogList(List<ApiRequestLog> requests, Pagination pagination) {}
