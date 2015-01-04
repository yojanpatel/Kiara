package uk.co.yojan.kiara.server.serializers;


import com.google.gson.*;
import uk.co.yojan.kiara.server.models.SongAnalysis;

import java.lang.reflect.Type;
import java.util.logging.Logger;

public class SongMetaDataDeserializer implements JsonDeserializer<SongAnalysis> {
  @Override
  public SongAnalysis deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    try {
      // Deserialize the metadata and audio summary section of the DataAnalysis object.
      SongAnalysis s = new SongAnalysis();
      JsonObject trackObj = jsonElement.getAsJsonObject().get("response").getAsJsonObject().get("track").getAsJsonObject();
      s.setTitle(trackObj.get("title").getAsString());

      if (trackObj.has("audio_md5")) {
        s.setAudioMd5(trackObj.get("audio_md5").getAsString());
      }
      s.setRelease(trackObj.get("release").getAsString());
      s.setArtist(trackObj.get("artist").getAsString());
      s.setStatus(trackObj.get("status").getAsString());
      s.setBasicMetaData(true);

      JsonObject audioSumObj = trackObj.get("audio_summary").getAsJsonObject();
      try {
        s.setTimeSignature(audioSumObj.get("time_signature").getAsInt());
      } catch (Exception e) {
        s.setTimeSignature(0);
      }

      try {
        s.setTempo(audioSumObj.get("tempo").getAsDouble());
      } catch (Exception e) {
        s.setTempo(0.0);
      }
      s.setEnergy(audioSumObj.get("energy").getAsDouble());
      s.setLiveness(audioSumObj.get("liveness").getAsDouble());
      try {
        s.setSpeechiness(audioSumObj.get("speechiness").getAsDouble());
      } catch (Exception e) {
        s.setSpeechiness(0.0);
      }
      s.setMode(audioSumObj.get("mode").getAsInt());
      s.setAcousticness(audioSumObj.get("acousticness").getAsDouble());
      s.setDanceability(audioSumObj.get("danceability").getAsDouble());
      s.setKey(audioSumObj.get("key").getAsInt());
      s.setDuration(audioSumObj.get("duration").getAsDouble());
      s.setLoudness(audioSumObj.get("loudness").getAsDouble());
      s.setValence(audioSumObj.get("valence").getAsDouble());
      s.setInstrumentalness(audioSumObj.get("instrumentalness").getAsDouble());
      s.setAnalysisUrl(audioSumObj.get("analysis_url").getAsString());
      s.setAudioSummary(true);

      return s;
    } catch (Exception e) {
      Logger.getLogger("parse").warning(e.getMessage());
      return null;
    }
  }
}
