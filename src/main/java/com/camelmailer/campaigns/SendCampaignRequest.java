package com.camelmailer.campaigns;

/**
 * Fields for {@link Campaigns#createAndSend}.
 *
 * <p>There is no schedule here: the send starts before the call returns.
 */
public final class SendCampaignRequest {

  private final String name;
  private final String from;
  private final String subject;
  private final String htmlBody;
  private final String textBody;

  private SendCampaignRequest(Builder builder) {
    this.name = builder.name;
    this.from = builder.from;
    this.subject = builder.subject;
    this.htmlBody = builder.htmlBody;
    this.textBody = builder.textBody;
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
   * Returns the bare sender address.
   *
   * @return the bare sender address, or {@code null} when unset
   */
  public String getFrom() {
    return from;
  }

  /**
   * Returns the message subject.
   *
   * @return the message subject, or {@code null} when unset
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the HTML part.
   *
   * @return the HTML part, or {@code null} when unset
   */
  public String getHtmlBody() {
    return htmlBody;
  }

  /**
   * Returns the plain-text part.
   *
   * @return the plain-text part, or {@code null} when unset
   */
  public String getTextBody() {
    return textBody;
  }

  /** Builder for {@link SendCampaignRequest}. */
  public static final class Builder {

    private String name;
    private String from;
    private String subject;
    private String htmlBody;
    private String textBody;

    private Builder() {}

    /**
     * Sets the display name (required).
     *
     * @param name the display name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the bare sender address.
     *
     * @param from the bare sender address
     * @return this builder
     */
    public Builder from(String from) {
      this.from = from;
      return this;
    }

    /**
     * Sets the message subject.
     *
     * @param subject the message subject
     * @return this builder
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the HTML part.
     *
     * @param htmlBody the HTML part
     * @return this builder
     */
    public Builder htmlBody(String htmlBody) {
      this.htmlBody = htmlBody;
      return this;
    }

    /**
     * Sets the plain-text part.
     *
     * @param textBody the plain-text part
     * @return this builder
     */
    public Builder textBody(String textBody) {
      this.textBody = textBody;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public SendCampaignRequest build() {
      return new SendCampaignRequest(this);
    }
  }
}
