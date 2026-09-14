package com.camelmailer.campaigns;

/**
 * Fields for {@link Campaigns#createDraft}.
 *
 * <p>The initial status follows what you pass: {@code sendNow} wins, then a {@code scheduledAt}
 * (status {@code scheduled}), else a draft.
 */
public final class DraftCampaignRequest {

  private final String stream;
  private final String from;
  private final String name;
  private final String subject;
  private final String htmlBody;
  private final String textBody;
  private final String scheduledAt;
  private final Boolean sendNow;

  private DraftCampaignRequest(Builder builder) {
    this.stream = builder.stream;
    this.from = builder.from;
    this.name = builder.name;
    this.subject = builder.subject;
    this.htmlBody = builder.htmlBody;
    this.textBody = builder.textBody;
    this.scheduledAt = builder.scheduledAt;
    this.sendNow = builder.sendNow;
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
   * Returns the permalink of the broadcast stream to send to.
   *
   * @return the permalink of the broadcast stream to send to, or {@code null} when unset
   */
  public String getStream() {
    return stream;
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
   * Returns the display name.
   *
   * @return the display name, or {@code null} when unset
   */
  public String getName() {
    return name;
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

  /**
   * Returns the RFC 3339 send time, which arms the campaign as scheduled.
   *
   * @return the RFC 3339 send time, which arms the campaign as scheduled, or {@code null} when
   *     unset
   */
  public String getScheduledAt() {
    return scheduledAt;
  }

  /**
   * Returns whether to send on creation, overriding the schedule.
   *
   * @return whether to send on creation, overriding the schedule, or {@code null} when unset
   */
  public Boolean getSendNow() {
    return sendNow;
  }

  /** Builder for {@link DraftCampaignRequest}. */
  public static final class Builder {

    private String stream;
    private String from;
    private String name;
    private String subject;
    private String htmlBody;
    private String textBody;
    private String scheduledAt;
    private Boolean sendNow;

    private Builder() {}

    /**
     * Sets the permalink of the broadcast stream to send to (required).
     *
     * @param stream the permalink of the broadcast stream to send to
     * @return this builder
     */
    public Builder stream(String stream) {
      this.stream = stream;
      return this;
    }

    /**
     * Sets the bare sender address (required).
     *
     * @param from the bare sender address
     * @return this builder
     */
    public Builder from(String from) {
      this.from = from;
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
     * Sets the RFC 3339 send time, which arms the campaign as scheduled.
     *
     * @param scheduledAt the RFC 3339 send time, which arms the campaign as scheduled
     * @return this builder
     */
    public Builder scheduledAt(String scheduledAt) {
      this.scheduledAt = scheduledAt;
      return this;
    }

    /**
     * Sets whether to send on creation, overriding the schedule.
     *
     * @param sendNow whether to send on creation, overriding the schedule
     * @return this builder
     */
    public Builder sendNow(Boolean sendNow) {
      this.sendNow = sendNow;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public DraftCampaignRequest build() {
      return new DraftCampaignRequest(this);
    }
  }
}
