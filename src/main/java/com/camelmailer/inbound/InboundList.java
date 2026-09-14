package com.camelmailer.inbound;

import com.camelmailer.common.Pagination;
import com.camelmailer.emails.Email;
import java.util.List;

/**
 * One page of inbound and held messages.
 *
 * @param inbound the page of messages; the API names this key {@code inbound}, not {@code messages}
 * @param pagination the page window
 */
public record InboundList(List<Email> inbound, Pagination pagination) {}
