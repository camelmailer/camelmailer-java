package com.camelmailer;

import com.camelmailer.bounces.Bounces;
import com.camelmailer.dmarc.Dmarc;
import com.camelmailer.emails.Emails;
import com.camelmailer.http.ApiClient;
import com.camelmailer.stats.Stats;
import com.camelmailer.streams.Streams;
import com.camelmailer.templates.Templates;
import java.net.http.HttpClient;

/**
 * The CamelMailer client — the entry point of the SDK.
 *
 * <pre>{@code
 * CamelMailer client = new CamelMailer("cm_xxx");
 *
 * SendResult result =
 *     client
 *         .emails()
 *         .send(
 *             SendEmailRequest.builder()
 *                 .from("billing@acme.com")
 *                 .to("ada@example.com")
 *                 .subject("Your receipt")
 *                 .textBody("Thanks!")
 *                 .build());
 * }</pre>
 *
 * <p>Self-hosted instances configure their own base URL:
 *
 * <pre>{@code
 * CamelMailer client =
 *     CamelMailer.builder().apiKey("cm_xxx").baseUrl("https://mail.example.com").build();
 * }</pre>
 *
 * <p>All methods are synchronous and thread-safe; API failures throw {@link CamelMailerException}.
 */
public final class CamelMailer {

  /** The base URL of the CamelMailer cloud, used when none is configured. */
  public static final String DEFAULT_BASE_URL = "https://app.camelmailer.com";

  private final Emails emails;
  private final Templates templates;
  private final Streams streams;
  private final Stats stats;
  private final Bounces bounces;
  private final Dmarc dmarc;

  /**
   * Creates a client for the CamelMailer cloud ({@value #DEFAULT_BASE_URL}).
   *
   * @param apiKey the server API key (sent as {@code X-Server-API-Key})
   */
  public CamelMailer(String apiKey) {
    this(builder().apiKey(apiKey));
  }

  private CamelMailer(Builder builder) {
    if (builder.apiKey == null || builder.apiKey.isBlank()) {
      throw new IllegalArgumentException("apiKey must not be null or blank");
    }
    HttpClient http = builder.httpClient != null ? builder.httpClient : HttpClient.newHttpClient();
    String baseUrl = builder.baseUrl != null ? builder.baseUrl : DEFAULT_BASE_URL;
    ApiClient api = new ApiClient(http, baseUrl, builder.apiKey);
    this.emails = new Emails(api);
    this.templates = new Templates(api);
    this.streams = new Streams(api);
    this.stats = new Stats(api);
    this.bounces = new Bounces(api);
    this.dmarc = new Dmarc(api);
  }

  /**
   * Creates a new client builder.
   *
   * @return a fresh builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Send and read emails.
   *
   * @return the emails service
   */
  public Emails emails() {
    return emails;
  }

  /**
   * Manage message templates.
   *
   * @return the templates service
   */
  public Templates templates() {
    return templates;
  }

  /**
   * Manage message streams.
   *
   * @return the streams service
   */
  public Streams streams() {
    return streams;
  }

  /**
   * Message and delivery statistics.
   *
   * @return the stats service
   */
  public Stats stats() {
    return stats;
  }

  /**
   * Read bounced messages.
   *
   * @return the bounces service
   */
  public Bounces bounces() {
    return bounces;
  }

  /**
   * DMARC compliance reporting.
   *
   * @return the DMARC service
   */
  public Dmarc dmarc() {
    return dmarc;
  }

  /** Builder for {@link CamelMailer}. */
  public static final class Builder {

    private String apiKey;
    private String baseUrl;
    private HttpClient httpClient;

    private Builder() {}

    /**
     * Sets the server API key (required).
     *
     * @param apiKey the API key
     * @return this builder
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Sets the instance base URL, e.g. {@code https://mail.example.com} for self-hosted
     * installations. Defaults to {@value CamelMailer#DEFAULT_BASE_URL}.
     *
     * @param baseUrl the base URL
     * @return this builder
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sets a custom JDK {@link HttpClient} (proxy, timeouts, executor, ...).
     *
     * @param httpClient the HTTP client to use
     * @return this builder
     */
    public Builder httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    /**
     * Builds the client.
     *
     * @return the configured client
     * @throws IllegalArgumentException when no API key is set
     */
    public CamelMailer build() {
      return new CamelMailer(this);
    }
  }
}
