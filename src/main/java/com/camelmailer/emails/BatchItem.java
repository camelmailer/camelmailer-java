package com.camelmailer.emails;

import com.camelmailer.common.ErrorInfo;

/**
 * One entry of a batch send result. Exactly one of {@link #data()} (on success) or {@link #error()}
 * (on failure) is set.
 *
 * @param status {@code success} or {@code error}
 * @param data the send result when this entry succeeded, else {@code null}
 * @param error the error when this entry failed, else {@code null}
 */
public record BatchItem(String status, SendResult data, ErrorInfo error) {

  /**
   * Whether this batch entry was queued successfully.
   *
   * @return {@code true} when {@link #status()} is {@code success}
   */
  public boolean isSuccess() {
    return "success".equals(status);
  }
}
