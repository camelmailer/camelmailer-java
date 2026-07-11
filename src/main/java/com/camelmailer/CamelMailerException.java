package com.camelmailer;

/**
 * Thrown when a CamelMailer API call fails.
 *
 * <p>Carries the stable, machine-readable error {@link #getCode() code} from the API's error
 * envelope (for example {@code Unauthorized}, {@code NotFound}, {@code ValidationError}, {@code
 * ParameterMissing}), the human-readable message, and the HTTP status code of the response.
 *
 * <p>Transport-level failures (connection refused, timeouts, unparseable responses) are reported
 * with the synthetic codes {@code ConnectionError} and {@code InvalidResponse} and a status code of
 * {@code 0}.
 */
public class CamelMailerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String code;
  private final int statusCode;

  /**
   * Creates a new exception.
   *
   * @param code stable error code, e.g. {@code ValidationError}
   * @param message human-readable error message
   * @param statusCode HTTP status code of the response, or {@code 0} for transport failures
   */
  public CamelMailerException(String code, String message, int statusCode) {
    super(message);
    this.code = code;
    this.statusCode = statusCode;
  }

  /**
   * Creates a new exception with a cause.
   *
   * @param code stable error code
   * @param message human-readable error message
   * @param statusCode HTTP status code, or {@code 0} for transport failures
   * @param cause the underlying cause
   */
  public CamelMailerException(String code, String message, int statusCode, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.statusCode = statusCode;
  }

  /**
   * Returns the stable, machine-readable error code (e.g. {@code Unauthorized}, {@code NotFound},
   * {@code ValidationError}).
   *
   * @return the error code
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the HTTP status code of the failed response, or {@code 0} when the request never
   * produced an HTTP response (connection error).
   *
   * @return the HTTP status code
   */
  public int getStatusCode() {
    return statusCode;
  }
}
