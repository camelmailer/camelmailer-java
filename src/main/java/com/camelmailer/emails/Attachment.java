package com.camelmailer.emails;

import java.util.Base64;

/**
 * A file attachment for an outgoing email.
 *
 * @param name file name, e.g. {@code invoice.pdf}
 * @param contentType MIME type, e.g. {@code application/pdf}
 * @param dataBase64 Base64-encoded file content
 */
public record Attachment(String name, String contentType, String dataBase64) {

  /**
   * Creates an attachment from raw bytes, Base64-encoding them.
   *
   * @param name file name
   * @param contentType MIME type
   * @param data raw file content
   * @return the attachment
   */
  public static Attachment of(String name, String contentType, byte[] data) {
    return new Attachment(name, contentType, Base64.getEncoder().encodeToString(data));
  }
}
