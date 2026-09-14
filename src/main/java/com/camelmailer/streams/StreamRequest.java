package com.camelmailer.streams;

/**
 * Parameters for creating or updating a message stream.
 *
 * <pre>{@code
 * StreamRequest request = StreamRequest.builder().name("Receipts").build();
 * }</pre>
 */
public final class StreamRequest {

  private final String name;
  private final String streamType;
  private final String permalink;
  private final Boolean archived;

  private StreamRequest(Builder builder) {
    this.name = builder.name;
    this.streamType = builder.streamType;
    this.permalink = builder.permalink;
    this.archived = builder.archived;
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
   * Returns the stream name.
   *
   * @return the name, or {@code null} when unset (update only)
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the stream type.
   *
   * @return {@code transactional} or {@code broadcast}, or {@code null} when unset
   */
  public String getStreamType() {
    return streamType;
  }

  /**
   * Returns the permalink.
   *
   * @return the permalink, or {@code null} when unset; the API derives one from the name then
   */
  public String getPermalink() {
    return permalink;
  }

  /**
   * Returns the archived flag.
   *
   * @return {@code true}, {@code false}, or {@code null} when unset (update only)
   */
  public Boolean getArchived() {
    return archived;
  }

  /** Builder for {@link StreamRequest}. */
  public static final class Builder {

    private String name;
    private String streamType;
    private String permalink;
    private Boolean archived;

    private Builder() {}

    /**
     * Sets the stream name (required on create).
     *
     * @param name display name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the stream type.
     *
     * @param streamType {@code transactional} or {@code broadcast}
     * @return this builder
     */
    public Builder streamType(String streamType) {
      this.streamType = streamType;
      return this;
    }

    /**
     * Sets the permalink used in API paths and send requests.
     *
     * @param permalink URL-safe identifier; the API derives one from the name when omitted
     * @return this builder
     */
    public Builder permalink(String permalink) {
      this.permalink = permalink;
      return this;
    }

    /**
     * Archives or unarchives the stream (update only). Archived streams reject new messages.
     *
     * @param archived whether the stream is archived
     * @return this builder
     */
    public Builder archived(boolean archived) {
      this.archived = archived;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public StreamRequest build() {
      return new StreamRequest(this);
    }
  }
}
