package com.camelmailer.subscribers;

/**
 * Fields for {@link Subscribers#add}.
 *
 * <p>Upserts by address, so calling it twice is safe.
 */
public final class SubscriberRequest {

  private final String address;
  private final String name;
  private final String status;

  private SubscriberRequest(Builder builder) {
    this.address = builder.address;
    this.name = builder.name;
    this.status = builder.status;
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
   * Returns the email address.
   *
   * @return the email address, or {@code null} when unset
   */
  public String getAddress() {
    return address;
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
   * Returns {@code subscribed} (default) or {@code unsubscribed}.
   *
   * @return {@code subscribed} (default) or {@code unsubscribed}, or {@code null} when unset
   */
  public String getStatus() {
    return status;
  }

  /** Builder for {@link SubscriberRequest}. */
  public static final class Builder {

    private String address;
    private String name;
    private String status;

    private Builder() {}

    /**
     * Sets the email address (required).
     *
     * @param address the email address
     * @return this builder
     */
    public Builder address(String address) {
      this.address = address;
      return this;
    }

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
     * Sets {@code subscribed} (default) or {@code unsubscribed}.
     *
     * @param status {@code subscribed} (default) or {@code unsubscribed}
     * @return this builder
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public SubscriberRequest build() {
      return new SubscriberRequest(this);
    }
  }
}
