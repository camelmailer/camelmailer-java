package com.camelmailer.dmarc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters and pagination for the DMARC endpoints.
 *
 * <pre>{@code
 * DmarcQuery query = DmarcQuery.builder().domain("acme.com").from("2026-01-01T00:00:00Z").build();
 * }</pre>
 */
public final class DmarcQuery {

  private final String domain;
  private final String from;
  private final String to;
  private final Integer page;
  private final Integer perPage;

  private DmarcQuery(Builder builder) {
    this.domain = builder.domain;
    this.from = builder.from;
    this.to = builder.to;
    this.page = builder.page;
    this.perPage = builder.perPage;
  }

  /**
   * Creates a new builder.
   *
   * @return a fresh builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Converts the query to query parameters, skipping unset values.
   *
   * @return query parameter map
   */
  public Map<String, String> toQuery() {
    Map<String, String> query = new LinkedHashMap<>();
    query.put("domain", domain);
    query.put("from", from);
    query.put("to", to);
    query.put("page", page == null ? null : page.toString());
    query.put("per_page", perPage == null ? null : perPage.toString());
    return query;
  }

  /** Builder for {@link DmarcQuery}. */
  public static final class Builder {

    private String domain;
    private String from;
    private String to;
    private Integer page;
    private Integer perPage;

    private Builder() {}

    /**
     * Filters by reported domain.
     *
     * @param domain the domain
     * @return this builder
     */
    public Builder domain(String domain) {
      this.domain = domain;
      return this;
    }

    /**
     * Matches reports whose date range overlaps a window starting here.
     *
     * @param from window start (RFC 3339)
     * @return this builder
     */
    public Builder from(String from) {
      this.from = from;
      return this;
    }

    /**
     * Matches reports whose date range overlaps a window ending here.
     *
     * @param to window end (RFC 3339)
     * @return this builder
     */
    public Builder to(String to) {
      this.to = to;
      return this;
    }

    /**
     * Sets the page (1-based; reports list only).
     *
     * @param page page number
     * @return this builder
     */
    public Builder page(int page) {
      this.page = page;
      return this;
    }

    /**
     * Sets the page size (max 100; reports list only).
     *
     * @param perPage items per page
     * @return this builder
     */
    public Builder perPage(int perPage) {
      this.perPage = perPage;
      return this;
    }

    /**
     * Builds the query.
     *
     * @return the immutable query
     */
    public DmarcQuery build() {
      return new DmarcQuery(this);
    }
  }
}
