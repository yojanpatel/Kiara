package uk.co.yojan.kiara.server.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.co.yojan.kiara.server.models.User;

import java.io.IOException;

public class UserSerializer extends JsonSerializer<User> {
  @Override
  public void serialize(User user, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException, JsonProcessingException {

    jsonGenerator.writeStartObject();

    jsonGenerator.writeStringField("id", user.getId());
    jsonGenerator.writeStringField("firstName", user.getFirstName());
    jsonGenerator.writeStringField("lastName", user.getLastName());
    jsonGenerator.writeStringField("facebookId", user.getFacebookId());
    jsonGenerator.writeStringField("email", user.getEmail());
    jsonGenerator.writeStringField("imageURL", user.getImageURL());
    jsonGenerator.writeNumberField("v", user.v());

    jsonGenerator.writeEndObject();
  }
}
