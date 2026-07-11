package com.camelmailer.emails;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;

/**
 * An email address, optionally with a display name.
 *
 * <p>Serializes to a bare string ({@code "ada@example.com"}) when no name is set, or to an object
 * ({@code {"email": "...", "name": "..."}}) when one is — matching the API's {@code Address}
 * schema.
 *
 * @param email the email address
 * @param name optional display name, may be {@code null}
 */
@JsonSerialize(using = Address.Serializer.class)
@JsonDeserialize(using = Address.Deserializer.class)
public record Address(String email, String name) {

  /**
   * Creates an address without a display name.
   *
   * @param email the email address
   * @return the address
   */
  public static Address of(String email) {
    return new Address(email, null);
  }

  /**
   * Creates an address with a display name.
   *
   * @param email the email address
   * @param name the display name
   * @return the address
   */
  public static Address of(String email, String name) {
    return new Address(email, name);
  }

  /** Serializes an {@link Address} as a bare string or a {@code {email, name}} object. */
  static final class Serializer extends com.fasterxml.jackson.databind.JsonSerializer<Address> {
    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value.name() == null) {
        gen.writeString(value.email());
      } else {
        gen.writeStartObject();
        gen.writeStringField("email", value.email());
        gen.writeStringField("name", value.name());
        gen.writeEndObject();
      }
    }
  }

  /** Deserializes an {@link Address} from either representation. */
  static final class Deserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Address> {
    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isTextual()) {
        return new Address(node.asText(), null);
      }
      String email = node.path("email").asText(null);
      String name = node.hasNonNull("name") ? node.get("name").asText() : null;
      return new Address(email, name);
    }
  }
}
