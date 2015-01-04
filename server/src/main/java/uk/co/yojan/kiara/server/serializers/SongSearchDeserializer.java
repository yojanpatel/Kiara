package uk.co.yojan.kiara.server.serializers;

import com.google.gson.*;
import uk.co.yojan.kiara.server.models.SongAnalysis;

import java.lang.reflect.Type;
import java.util.logging.Logger;

public class SongSearchDeserializer implements JsonDeserializer<SongAnalysis> {


  @Override
  public SongAnalysis deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    SongAnalysis songAnalysis = new SongAnalysis();

    try {
      JsonArray songObj = jsonElement.getAsJsonObject().get("response").getAsJsonObject().get("songs").getAsJsonArray();


      JsonObject songJson = songObj.get(0).getAsJsonObject();

      songAnalysis.setArtist(songJson.get("artist_name").getAsString());

      if (songJson.has("release"))
        songAnalysis.setRelease(songJson.get("release").getAsString());
      Logger.getGlobal().warning(songJson.toString());
      songAnalysis.setTitle(songJson.get("title").getAsString());

      // No point if there is no data.
      if (!songJson.has("audio_summary")) {
        return null;
      }

      JsonObject as = songJson.get("audio_summary").getAsJsonObject();

      songAnalysis.setAudioMd5(as.get("audio_md5").getAsString());
      songAnalysis.setAcousticness(as.get("acousticness").getAsDouble());
      songAnalysis.setInstrumentalness(as.get("instrumentalness").getAsDouble());
      songAnalysis.setDanceability(as.get("danceability").getAsDouble());
      songAnalysis.setTempo(as.get("tempo").getAsDouble());
      songAnalysis.setDuration(as.get("duration").getAsDouble());
      songAnalysis.setTimeSignature(as.get("time_signature").getAsInt());
      songAnalysis.setLiveness(as.get("liveness").getAsDouble());
      songAnalysis.setSpeechiness(as.get("speechiness").getAsDouble());
      songAnalysis.setMode(as.get("mode").getAsInt());
      songAnalysis.setKey(as.get("key").getAsInt());
      songAnalysis.setValence(as.get("valence").getAsDouble());
      songAnalysis.setEnergy(as.get("energy").getAsDouble());
      songAnalysis.setAnalysisUrl(as.get("analysis_url").getAsString());

      return songAnalysis;
    } catch(Exception e) {
      Logger.getLogger("e").warning(e.getMessage());
      return null;
    }
  }
}
