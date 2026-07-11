package com.camelmailer.emails;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameters for sending an email — used by {@link Emails#send}, {@link Emails#sendBatch}, and
 * (with {@link Builder#template template} set) {@link Emails#sendWithTemplate}.
 *
 * <p>Build via {@link #builder()}:
 *
 * <pre>{@code
 * SendEmailRequest request =
 *     SendEmailRequest.builder()
 *         .from("billing@acme.com")
 *         .to("ada@example.com")
 *         .subject("Your receipt")
 *         .textBody("Thanks for your purchase.")
 *         .tag("receipt")
 *         .build();
 * }</pre>
 */
public final class SendEmailRequest {

  private final Address from;
  private final List<Address> to;
  private final List<Address> cc;
  private final List<Address> bcc;
  private final List<Address> replyTo;
  private final String subject;
  private final String htmlBody;
  private final String textBody;
  private final Map<String, String> headers;
  private final List<Attachment> attachments;
  private final String tag;
  private final Map<String, Object> metadata;
  private final String stream;
  private final String template;
  private final Map<String, Object> templateModel;

  private SendEmailRequest(Builder builder) {
    this.from = builder.from;
    this.to = builder.to.isEmpty() ? null : List.copyOf(builder.to);
    this.cc = builder.cc.isEmpty() ? null : List.copyOf(builder.cc);
    this.bcc = builder.bcc.isEmpty() ? null : List.copyOf(builder.bcc);
    this.replyTo = builder.replyTo.isEmpty() ? null : List.copyOf(builder.replyTo);
    this.subject = builder.subject;
    this.htmlBody = builder.htmlBody;
    this.textBody = builder.textBody;
    this.headers = builder.headers.isEmpty() ? null : Map.copyOf(builder.headers);
    this.attachments = builder.attachments.isEmpty() ? null : List.copyOf(builder.attachments);
    this.tag = builder.tag;
    this.metadata = builder.metadata;
    this.stream = builder.stream;
    this.template = builder.template;
    this.templateModel = builder.templateModel;
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
   * Returns the sender address.
   *
   * @return the sender
   */
  public Address getFrom() {
    return from;
  }

  /**
   * Returns the recipient addresses.
   *
   * @return recipients, or {@code null} when unset
   */
  public List<Address> getTo() {
    return to;
  }

  /**
   * Returns the CC addresses.
   *
   * @return CC recipients, or {@code null} when unset
   */
  public List<Address> getCc() {
    return cc;
  }

  /**
   * Returns the BCC addresses.
   *
   * @return BCC recipients, or {@code null} when unset
   */
  public List<Address> getBcc() {
    return bcc;
  }

  /**
   * Returns the Reply-To addresses.
   *
   * @return Reply-To addresses, or {@code null} when unset
   */
  public List<Address> getReplyTo() {
    return replyTo;
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
   * Returns the HTML body.
   *
   * @return the HTML body, or {@code null} when unset
   */
  public String getHtmlBody() {
    return htmlBody;
  }

  /**
   * Returns the plain-text body.
   *
   * @return the text body, or {@code null} when unset
   */
  public String getTextBody() {
    return textBody;
  }

  /**
   * Returns extra message headers.
   *
   * @return headers, or {@code null} when unset
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Returns the attachments.
   *
   * @return attachments, or {@code null} when unset
   */
  public List<Attachment> getAttachments() {
    return attachments;
  }

  /**
   * Returns the free-form tag used for filtering and stats.
   *
   * @return the tag, or {@code null} when unset
   */
  public String getTag() {
    return tag;
  }

  /**
   * Returns the metadata attached to the message.
   *
   * @return metadata, or {@code null} when unset
   */
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  /**
   * Returns the message-stream permalink.
   *
   * @return the stream permalink, or {@code null} for the server's default stream
   */
  public String getStream() {
    return stream;
  }

  /**
   * Returns the template permalink (only used by template sends).
   *
   * @return the template permalink, or {@code null}
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Returns the model the template is rendered against.
   *
   * @return the template model, or {@code null}
   */
  public Map<String, Object> getTemplateModel() {
    return templateModel;
  }

  /** Builder for {@link SendEmailRequest}. */
  public static final class Builder {

    private Address from;
    private final List<Address> to = new ArrayList<>();
    private final List<Address> cc = new ArrayList<>();
    private final List<Address> bcc = new ArrayList<>();
    private final List<Address> replyTo = new ArrayList<>();
    private String subject;
    private String htmlBody;
    private String textBody;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<Attachment> attachments = new ArrayList<>();
    private String tag;
    private Map<String, Object> metadata;
    private String stream;
    private String template;
    private Map<String, Object> templateModel;

    private Builder() {}

    /**
     * Sets the sender address. Its domain must be a verified sending domain of the server.
     *
     * @param email sender email address
     * @return this builder
     */
    public Builder from(String email) {
      this.from = Address.of(email);
      return this;
    }

    /**
     * Sets the sender address with a display name.
     *
     * @param email sender email address
     * @param name display name
     * @return this builder
     */
    public Builder from(String email, String name) {
      this.from = Address.of(email, name);
      return this;
    }

    /**
     * Sets the sender address.
     *
     * @param address sender address
     * @return this builder
     */
    public Builder from(Address address) {
      this.from = address;
      return this;
    }

    /**
     * Adds recipient addresses.
     *
     * @param emails recipient email addresses
     * @return this builder
     */
    public Builder to(String... emails) {
      for (String email : emails) {
        this.to.add(Address.of(email));
      }
      return this;
    }

    /**
     * Adds a recipient address.
     *
     * @param address recipient address
     * @return this builder
     */
    public Builder to(Address address) {
      this.to.add(address);
      return this;
    }

    /**
     * Adds CC addresses.
     *
     * @param emails CC email addresses
     * @return this builder
     */
    public Builder cc(String... emails) {
      for (String email : emails) {
        this.cc.add(Address.of(email));
      }
      return this;
    }

    /**
     * Adds a CC address.
     *
     * @param address CC address
     * @return this builder
     */
    public Builder cc(Address address) {
      this.cc.add(address);
      return this;
    }

    /**
     * Adds BCC addresses.
     *
     * @param emails BCC email addresses
     * @return this builder
     */
    public Builder bcc(String... emails) {
      for (String email : emails) {
        this.bcc.add(Address.of(email));
      }
      return this;
    }

    /**
     * Adds a BCC address.
     *
     * @param address BCC address
     * @return this builder
     */
    public Builder bcc(Address address) {
      this.bcc.add(address);
      return this;
    }

    /**
     * Adds Reply-To addresses.
     *
     * @param emails Reply-To email addresses
     * @return this builder
     */
    public Builder replyTo(String... emails) {
      for (String email : emails) {
        this.replyTo.add(Address.of(email));
      }
      return this;
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject line
     * @return this builder
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the HTML body.
     *
     * @param htmlBody HTML content
     * @return this builder
     */
    public Builder htmlBody(String htmlBody) {
      this.htmlBody = htmlBody;
      return this;
    }

    /**
     * Sets the plain-text body.
     *
     * @param textBody plain-text content
     * @return this builder
     */
    public Builder textBody(String textBody) {
      this.textBody = textBody;
      return this;
    }

    /**
     * Adds an extra message header.
     *
     * @param name header name
     * @param value header value
     * @return this builder
     */
    public Builder header(String name, String value) {
      this.headers.put(name, value);
      return this;
    }

    /**
     * Adds an attachment.
     *
     * @param attachment the attachment
     * @return this builder
     */
    public Builder attachment(Attachment attachment) {
      this.attachments.add(attachment);
      return this;
    }

    /**
     * Sets a free-form tag used for filtering and stats.
     *
     * @param tag the tag
     * @return this builder
     */
    public Builder tag(String tag) {
      this.tag = tag;
      return this;
    }

    /**
     * Attaches metadata to the message (returned in message details and webhooks).
     *
     * @param metadata arbitrary key/value data
     * @return this builder
     */
    public Builder metadata(Map<String, Object> metadata) {
      this.metadata = metadata;
      return this;
    }

    /**
     * Sets the message-stream permalink. Defaults to the server's default stream.
     *
     * @param stream stream permalink
     * @return this builder
     */
    public Builder stream(String stream) {
      this.stream = stream;
      return this;
    }

    /**
     * Sets the template permalink for {@link Emails#sendWithTemplate}.
     *
     * @param template template permalink
     * @return this builder
     */
    public Builder template(String template) {
      this.template = template;
      return this;
    }

    /**
     * Sets the model the template is rendered against.
     *
     * @param templateModel template variables
     * @return this builder
     */
    public Builder templateModel(Map<String, Object> templateModel) {
      this.templateModel = templateModel;
      return this;
    }

    /**
     * Builds the request.
     *
     * @return the immutable request
     */
    public SendEmailRequest build() {
      return new SendEmailRequest(this);
    }
  }
}
