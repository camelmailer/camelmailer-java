package com.camelmailer.emails;

import com.camelmailer.common.Pagination;
import java.util.List;

/**
 * A page of messages.
 *
 * @param messages the messages on this page
 * @param pagination pagination metadata
 */
public record EmailList(List<Email> messages, Pagination pagination) {}
