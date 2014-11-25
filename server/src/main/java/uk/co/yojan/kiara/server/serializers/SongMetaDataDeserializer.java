package uk.co.yojan.kiara.server.serializers;


import com.google.gson.*;
import uk.co.yojan.kiara.server.models.SongAnalysis;

import java.lang.reflect.Type;
import java.util.logging.Logger;

public class SongMetaDataDeserializer implements JsonDeserializer<SongAnalysis> {
  @Override
  public SongAnalysis deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

    // Deserialize the metadata and audio summary section of the DataAnalysis object.
    SongAnalysis s = new SongAnalysis();
    JsonObject trackObj = jsonElement.getAsJsonObject().get("response").getAsJsonObject().get("track").getAsJsonObject();
    s.setTitle(trackObj.get("title").getAsString());
    s.setAudioMd5(trackObj.get("audio_md5").getAsString());
    s.setRelease(trackObj.get("release").getAsString());
    s.setArtist(trackObj.get("artist").getAsString());
    s.setStatus(trackObj.get("status").getAsString());
    s.setBasicMetaData(true);

    JsonObject audioSumObj = trackObj.get("audio_summary").getAsJsonObject();
    s.setTimeSignature(audioSumObj.get("time_signature").getAsInt());
    s.setTempo(audioSumObj.get("tempo").getAsDouble());
    s.setEnergy(audioSumObj.get("energy").getAsDouble());
    s.setLiveness(audioSumObj.get("liveness").getAsDouble());
    s.setSpeechiness(audioSumObj.get("speechiness").getAsDouble());
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
  }
}
