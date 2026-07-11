package com.camelmailer.emails;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters and pagination for {@link Emails#list}.
 *
 * <pre>{@code
 * ListEmailsOptions options =
 *     ListEmailsOptions.builder().scope("outgoing").tag("receipt").page(2).build();
 * }</pre>
 */
public final class ListEmailsOptions {

  private final Integer page;
  private final Integer perPage;
  private final String scope;
  private final String status;
  private final String tag;
  private final String query;
  private final String stream;

  private ListEmailsOptions(Builder builder) {
    this.page = builder.page;
    this.perPage = builder.perPage;
    this.scope = builder.scope;
    this.status = builder.status;
    this.tag = builder.tag;
    this.query = builder.query;
    this.stream = builder.stream;
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
   * Converts the options to query parameters, skipping unset values.
   *
   * @return query parameter map
   */
  public Map<String, String> toQuery() {
    Map<String, String> query = new LinkedHashMap<>();
    if (page != null) {
      query.put("page", page.toString());
    }
    if (perPage != null) {
      query.put("per_page", perPage.toString());
    }
    query.put("scope", scope);
    query.put("status", status);
    query.put("tag", tag);
    query.put("query", this.query);
    query.put("stream", stream);
    return query;
  }

  /** Builder for {@link ListEmailsOptions}. */
  public static final class Builder {

    private Integer page;
    private Integer perPage;
    private String scope;
    private String status;
    private String tag;
    private String query;
    private String stream;

    private Builder() {}

    /**
     * Sets the page (1-based).
     *
     * @param page page number
     * @return this builder
     */
    public Builder page(int page) {
      this.page = page;
      return this;
    }

    /**
     * Sets the page size (max 100).
     *
     * @param perPage items per page
     * @return this builder
     */
    public Builder perPage(int perPage) {
      this.perPage = perPage;
      return this;
    }

    /**
     * Filters by scope: {@code incoming} or {@code outgoing}.
     *
     * @param scope the scope
     * @return this builder
     */
    public Builder scope(String scope) {
      this.scope = scope;
      return this;
    }

    /**
     * Filters by delivery status.
     *
     * @param status the status
     * @return this builder
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Filters by tag.
     *
     * @param tag the tag
     * @return this builder
     */
    public Builder tag(String tag) {
      this.tag = tag;
      return this;
    }

    /**
     * Substring match on subject and addresses.
     *
     * @param query the search string
     * @return this builder
     */
    public Builder query(String query) {
      this.query = query;
      return this;
    }

    /**
     * Filters by message-stream permalink.
     *
     * @param stream the stream permalink
     * @return this builder
     */
    public Builder stream(String stream) {
      this.stream = stream;
      return this;
    }

    /**
     * Builds the options.
     *
     * @return the immutable options
     */
    public ListEmailsOptions build() {
      return new ListEmailsOptions(this);
    }
  }
}
