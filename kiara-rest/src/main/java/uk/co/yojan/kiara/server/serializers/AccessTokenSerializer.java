package uk.co.yojan.kiara.server.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.co.yojan.kiara.server.models.AccessToken;

import java.io.IOException;

public class AccessTokenSerializer extends JsonSerializer<AccessToken> {
  @Override
  public void serialize(AccessToken accessToken, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("accessToken", accessToken.getAccessToken());
    jsonGenerator.writeNumberField("expiresIn", accessToken.getExpiresIn());
    jsonGenerator.writeEndObject();
  }
}
