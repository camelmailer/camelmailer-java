package com.camelmailer.layouts;

/**
 * Fields for creating or updating a template layout.
 *
 * <p>{@code htmlWrapper} is required when creating, and has to embed the body with <code>
 * {{{ content }}}</code>; anything else is refused with {@code ValidationError}.
 */
public final class LayoutRequest {

  private final String name;
  private final String permalink;
  private final String htmlWrapper;
  private final String textWrapper;

  private LayoutRequest(Builder builder) {
    this.name = builder.name;
    this.permalink = builder.permalink;
    this.htmlWrapper = builder.htmlWrapper;
    this.textWrapper = builder.textWrapper;
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
   * Returns the display name.
   *
   * @return the display name, or {@code null} when unset
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the URL-safe identifier, derived from the name when unset.
   *
   * @return the URL-safe identifier, derived from the name when unset, or {@code null} when unset
   */
  public String getPermalink() {
    return permalink;
  }

  /**
   * Returns the wrapper for the HTML body.
   *
   * @return the wrapper for the HTML body, or {@code null} when unset
   */
  public String getHtmlWrapper() {
    return htmlWrapper;
  }

  /**
   * Returns the wrapper for the plain-text body.
   *
   * @return the wrapper for the plain-text body, or {@code null} when unset
   */
  public String getTextWrapper() {
    return textWrapper;
  }

  /** Builder for {@link LayoutRequest}. */
  public static final class Builder {

    private String name;
    private String permalink;
    private String htmlWrapper;
    private String textWrapper;

    private Builder() {}

    /**
     * Sets the display name.
     *
     * @param name the display name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the URL-safe identifier, derived from the name when unset.
     *
     * @param permalink the URL-safe identifier, derived from the name when unset
     * @return this builder
     */
    public Builder permalink(String permalink) {
      this.permalink = permalink;
      return this;
    }

    /**
     * Sets the wrapper for the HTML body.
     *
     * @param htmlWrapper the wrapper for the HTML body
     * @return this builder
     */
    public Builder htmlWrapper(String htmlWrapper) {
      this.htmlWrapper = htmlWrapper;
      return this;
    }

    /**
     * Sets the wrapper for the plain-text body.
     *
     * @param textWrapper the wrapper for the plain-text body
     * @return this builder
     */
    public Builder textWrapper(String textWrapper) {
      this.textWrapper = textWrapper;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public LayoutRequest build() {
      return new LayoutRequest(this);
    }
  }
}
