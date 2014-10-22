package uk.co.yojan.kiara.server.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.co.yojan.kiara.server.models.Song;

import java.io.IOException;

/**
 * Created by yojan on 10/10/14.
 */
public class SongSerializer extends JsonSerializer<Song> {
  @Override
  public void serialize(Song song, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException, JsonProcessingException {

    jsonGenerator.writeStartObject();

//    jsonGenerator.writeNumberField("id", song.getId());
    jsonGenerator.writeStringField("spotifyId", song.getSpotifyId());
    jsonGenerator.writeStringField("songName", song.getSongName());
    jsonGenerator.writeStringField("artistName", song.getArtist());
    jsonGenerator.writeStringField("albumName", song.getAlbumName());
    jsonGenerator.writeStringField("imageURL", song.getImageURL());

    jsonGenerator.writeEndObject();
  }
}
