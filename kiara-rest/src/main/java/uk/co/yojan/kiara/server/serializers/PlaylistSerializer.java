package uk.co.yojan.kiara.server.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.co.yojan.kiara.server.models.Playlist;

import java.io.IOException;

/**
 * Created by yojan on 10/10/14.
 */
public class PlaylistSerializer extends JsonSerializer<Playlist> {
  @Override
  public void serialize(Playlist playlist, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException, JsonProcessingException {

    jsonGenerator.writeStartObject();

    jsonGenerator.writeNumberField("id", playlist.getId());
    jsonGenerator.writeStringField("playlistName", playlist.getName());
    jsonGenerator.writeNumberField("lastViewedTimestamp", playlist.getLastViewedTimestamp());

    jsonGenerator.writeEndObject();

  }
}
