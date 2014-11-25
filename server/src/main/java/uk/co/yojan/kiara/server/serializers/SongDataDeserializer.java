package uk.co.yojan.kiara.server.serializers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import uk.co.yojan.kiara.server.echonest.data.*;
import uk.co.yojan.kiara.server.models.SongData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SongDataDeserializer implements JsonDeserializer<SongData> {
  @Override
  public SongData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    // Deserialize to a SongData object, the results of the extended analysis request from EchoNest.

    SongData s = new SongData();
    Logger log = Logger.getLogger("Deserializer");

    JsonObject obj = jsonElement.getAsJsonObject();
    JsonObject trackObj = obj.get("track").getAsJsonObject();
    log.info(trackObj.toString());
    s.setNumSamples(trackObj.get("num_samples").getAsInt());
    s.setDuration(trackObj.get("duration").getAsDouble());
    s.setOffsetSeconds(trackObj.get("offset_seconds").getAsInt());
    s.setWindowSeconds(trackObj.get("window_seconds").getAsInt());
    s.setAnalysisSampleRate(trackObj.get("analysis_sample_rate").getAsInt());
    s.setAnalysisChannels(trackObj.get("analysis_channels").getAsInt());
    s.setEndOfFadeIn(trackObj.get("end_of_fade_in").getAsDouble());
    s.setStartOfFadeOut(trackObj.get("start_of_fade_out").getAsDouble());
    s.setLoudness(trackObj.get("loudness").getAsDouble());
    s.setTempo(trackObj.get("tempo").getAsDouble());
    s.setTempoConfidence(trackObj.get("tempo_confidence").getAsDouble());
    s.setTimeSignature(trackObj.get("time_signature").getAsInt());
    s.setTimeSignatureConfidence(trackObj.get("time_signature_confidence").getAsDouble());
    s.setKey(trackObj.get("key").getAsInt());
    s.setKeyConfidence(trackObj.get("key_confidence").getAsDouble());
    s.setMode(trackObj.get("mode").getAsInt());
    s.setModeConfidence(trackObj.get("mode_confidence").getAsDouble());

    Type barListType = new TypeToken<ArrayList<Bar>>(){}.getType();
    Type beatListType = new TypeToken<ArrayList<Beat>>(){}.getType();
    Type sectionListType = new TypeToken<ArrayList<Section>>(){}.getType();
    Type segmentListType = new TypeToken<ArrayList<Segment>>(){}.getType();
    Type tatumListType = new TypeToken<ArrayList<Tatum>>(){}.getType();

    Gson gson = new Gson();
    s.setBars(gson.<List<Bar>>fromJson(obj.getAsJsonArray("bars"), barListType));
    s.setBeats(gson.<List<Beat>>fromJson(obj.getAsJsonArray("beats"), barListType));
    s.setSections(gson.<List<Section>>fromJson(obj.getAsJsonArray("sections"), sectionListType));
    s.setSegments(gson.<List<Segment>>fromJson(obj.getAsJsonArray("segments"), segmentListType));
    s.setTatums(gson.<List<Tatum>>fromJson(obj.getAsJsonArray("tatums"), tatumListType));

    return s;
  }

}
