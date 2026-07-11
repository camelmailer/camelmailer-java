package com.camelmailer.common;

/**
 * Pagination metadata returned by list endpoints.
 *
 * @param page current page (1-based)
 * @param perPage items per page (max 100)
 * @param total total number of items
 * @param totalPages total number of pages
 */
public record Pagination(int page, int perPage, long total, int totalPages) {}
