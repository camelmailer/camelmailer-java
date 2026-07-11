package com.camelmailer.templates;

/**
 * Parameters for creating or updating a template.
 *
 * <pre>{@code
 * TemplateRequest request =
 *     TemplateRequest.builder()
 *         .name("Welcome")
 *         .subject("Welcome, {{ name }}!")
 *         .htmlBody("<h1>Hi {{ name }}</h1>")
 *         .build();
 * }</pre>
 */
public final class TemplateRequest {

  private final String name;
  private final String subject;
  private final String htmlBody;
  private final String textBody;

  private TemplateRequest(Builder builder) {
    this.name = builder.name;
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
   * Returns the template name.
   *
   * @return the name, or {@code null} when unset (update only)
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the subject template.
   *
   * @return the subject, or {@code null} when unset
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the HTML body template.
   *
   * @return the HTML body, or {@code null} when unset
   */
  public String getHtmlBody() {
    return htmlBody;
  }

  /**
   * Returns the text body template.
   *
   * @return the text body, or {@code null} when unset
   */
  public String getTextBody() {
    return textBody;
  }

  /** Builder for {@link TemplateRequest}. */
  public static final class Builder {

    private String name;
    private String subject;
    private String htmlBody;
    private String textBody;

    private Builder() {}

    /**
     * Sets the template name (required on create).
     *
     * @param name display name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the subject template.
     *
     * @param subject subject, may contain {@code {{ variables }}}
     * @return this builder
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the HTML body template.
     *
     * @param htmlBody HTML body
     * @return this builder
     */
    public Builder htmlBody(String htmlBody) {
      this.htmlBody = htmlBody;
      return this;
    }

    /**
     * Sets the text body template.
     *
     * @param textBody text body
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
    public TemplateRequest build() {
      return new TemplateRequest(this);
    }
  }
}
