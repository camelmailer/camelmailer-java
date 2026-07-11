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

  private StreamRequest(Builder builder) {
    this.name = builder.name;
    this.streamType = builder.streamType;
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

  /** Builder for {@link StreamRequest}. */
  public static final class Builder {

    private String name;
    private String streamType;

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
     * Builds the request.
     *
     * @return the immutable request
     */
    public StreamRequest build() {
      return new StreamRequest(this);
    }
  }
}
