package com.camelmailer.bounces;

import com.camelmailer.common.Pagination;
import com.camelmailer.emails.Email;
import java.util.List;

/**
 * A page of bounced messages.
 *
 * @param bounces the bounces on this page
 * @param pagination pagination metadata
 */
public record BounceList(List<Email> bounces, Pagination pagination) {}
