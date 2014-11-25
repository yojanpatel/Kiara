package uk.co.yojan.kiara.server.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.co.yojan.kiara.server.models.Playlist;

import java.io.IOException;


public class PlaylistDeserializer extends JsonDeserializer<Playlist> {
  @Override
  public Playlist deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    ObjectNode n = jsonParser.getCodec().readTree(jsonParser);

    Playlist p = new Playlist();
    p.setName(n.get("playlistName").asText());
    return p;
  }
}
