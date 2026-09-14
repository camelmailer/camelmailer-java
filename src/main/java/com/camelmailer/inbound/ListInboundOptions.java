package com.camelmailer.inbound;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters for {@link Inbound#list}. All optional.
 *
 * <pre>{@code
 * ListInboundOptions options = ListInboundOptions.builder().status("held").perPage(50).build();
 * }</pre>
 */
public final class ListInboundOptions {

  private final String status;
  private final String stream;
  private final String query;
  private final Integer page;
  private final Integer perPage;

  private ListInboundOptions(Builder builder) {
    this.status = builder.status;
    this.stream = builder.stream;
    this.query = builder.query;
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
   * Renders the filters as query parameters.
   *
   * @return the parameters; unset filters are omitted
   */
  public Map<String, String> toQuery() {
    Map<String, String> query = new LinkedHashMap<>();
    if (status != null) {
      query.put("status", status);
    }
    if (stream != null) {
      query.put("stream", stream);
    }
    if (this.query != null) {
      query.put("query", this.query);
    }
    if (page != null) {
      query.put("page", String.valueOf(page));
    }
    if (perPage != null) {
      query.put("per_page", String.valueOf(perPage));
    }
    return query;
  }

  /** Builder for {@link ListInboundOptions}. */
  public static final class Builder {

    private String status;
    private String stream;
    private String query;
    private Integer page;
    private Integer perPage;

    private Builder() {}

    /**
     * Restricts to one delivery status, e.g. {@code held}.
     *
     * @param status the status
     * @return this builder
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Restricts to one message stream.
     *
     * @param stream the stream permalink
     * @return this builder
     */
    public Builder stream(String stream) {
      this.stream = stream;
      return this;
    }

    /**
     * Substring match on subject and addresses.
     *
     * @param query the search term
     * @return this builder
     */
    public Builder query(String query) {
      this.query = query;
      return this;
    }

    /**
     * Sets the page number.
     *
     * @param page 1-based page number
     * @return this builder
     */
    public Builder page(int page) {
      this.page = page;
      return this;
    }

    /**
     * Sets the page size.
     *
     * @param perPage page size, capped at 100
     * @return this builder
     */
    public Builder perPage(int perPage) {
      this.perPage = perPage;
      return this;
    }

    /**
     * Builds the options.
     *
     * @return the immutable options
     */
    public ListInboundOptions build() {
      return new ListInboundOptions(this);
    }
  }
}
