package com.camelmailer.campaigns;

/**
 * Fields for {@link Campaigns#update}. Unset fields are left unchanged.
 *
 * <p>The schedule is three-valued, which is why it has two builder methods: touching neither keeps
 * the current schedule, {@code scheduledAt} moves a draft to {@code scheduled}, and {@code
 * clearSchedule} sends an explicit {@code null}, which drops the campaign back to {@code draft}. An
 * omitted field and a {@code null} mean different things to the API.
 */
public final class UpdateCampaignRequest {

  private final String name;
  private final String from;
  private final String subject;
  private final String htmlBody;
  private final String textBody;
  private final String scheduledAt;
  private final boolean clearSchedule;

  private UpdateCampaignRequest(Builder builder) {
    this.name = builder.name;
    this.from = builder.from;
    this.subject = builder.subject;
    this.htmlBody = builder.htmlBody;
    this.textBody = builder.textBody;
    this.scheduledAt = builder.scheduledAt;
    this.clearSchedule = builder.clearSchedule;
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
   * @return the name, or {@code null} when unset
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the sender address.
   *
   * @return the address, or {@code null} when unset
   */
  public String getFrom() {
    return from;
  }

  /**
   * Returns the subject.
   *
   * @return the subject, or {@code null} when unset
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the HTML part.
   *
   * @return the HTML body, or {@code null} when unset
   */
  public String getHtmlBody() {
    return htmlBody;
  }

  /**
   * Returns the plain-text part.
   *
   * @return the text body, or {@code null} when unset
   */
  public String getTextBody() {
    return textBody;
  }

  /**
   * Returns the schedule.
   *
   * @return the RFC 3339 send time, or {@code null} when unset or cleared
   */
  public String getScheduledAt() {
    return scheduledAt;
  }

  /**
   * Returns whether the update clears the schedule.
   *
   * <p>{@link Campaigns#update} turns this into an explicit {@code null} in the body; an omitted
   * field would leave the schedule standing.
   *
   * @return {@code true} when the schedule should be cleared
   */
  public boolean isClearSchedule() {
    return clearSchedule;
  }

  /** Builder for {@link UpdateCampaignRequest}. */
  public static final class Builder {

    private String name;
    private String from;
    private String subject;
    private String htmlBody;
    private String textBody;
    private String scheduledAt;
    private boolean clearSchedule;

    private Builder() {}

    /**
     * Sets the display name.
     *
     * @param name the name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the sender address.
     *
     * @param from the address
     * @return this builder
     */
    public Builder from(String from) {
      this.from = from;
      return this;
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     * @return this builder
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the HTML part.
     *
     * @param htmlBody the HTML body
     * @return this builder
     */
    public Builder htmlBody(String htmlBody) {
      this.htmlBody = htmlBody;
      return this;
    }

    /**
     * Sets the plain-text part.
     *
     * @param textBody the text body
     * @return this builder
     */
    public Builder textBody(String textBody) {
      this.textBody = textBody;
      return this;
    }

    /**
     * Schedules the campaign, moving a draft to {@code scheduled}.
     *
     * @param scheduledAt RFC 3339 send time
     * @return this builder
     */
    public Builder scheduledAt(String scheduledAt) {
      this.scheduledAt = scheduledAt;
      this.clearSchedule = false;
      return this;
    }

    /**
     * Clears the schedule, dropping the campaign back to {@code draft}.
     *
     * <p>This sends an explicit {@code null}; omitting the field would leave the schedule standing.
     *
     * @return this builder
     */
    public Builder clearSchedule() {
      this.scheduledAt = null;
      this.clearSchedule = true;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public UpdateCampaignRequest build() {
      return new UpdateCampaignRequest(this);
    }
  }
}
