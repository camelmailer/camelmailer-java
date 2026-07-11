package com.camelmailer.emails;

import java.util.List;

/**
 * A message with its delivery attempts, as returned by {@link Emails#get}.
 *
 * @param message the message
 * @param deliveries delivery attempts, newest first
 */
public record EmailDetails(Email message, List<Delivery> deliveries) {}
