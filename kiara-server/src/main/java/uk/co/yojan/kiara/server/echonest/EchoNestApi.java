package uk.co.yojan.kiara.server.echonest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.yojan.kiara.server.ApiConstants;
import uk.co.yojan.kiara.server.echonest.data.SongData;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.serializers.SongDataDeserializer;
import uk.co.yojan.kiara.server.serializers.SongMetaDataDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;



public class EchoNestApi {

  private static String baseURL = "http://developer.echonest.com/api/v4";
  private static String query = "?api_key=" + ApiConstants.ECHO_NEST_API_KEY;

  private static Gson gson = new GsonBuilder()
      .registerTypeAdapter(SongAnalysis.class, new SongMetaDataDeserializer())
      .registerTypeAdapter(SongData.class, new SongDataDeserializer())
      .create();

  public static String get(URL url) {
    try {
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      urlConn.setRequestMethod("GET");
      urlConn.setReadTimeout(10000); // 10 seconds
      StringWriter w = new StringWriter();
      InputStream is = urlConn.getInputStream();
      byte[] bytes = new byte[is.available()];
      is.read(bytes, 0, is.available());
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  // Get the raw JSON content from an EchoNest request given a spotifyId. (sync)
  private static String getSongMetaDataJson(String spotifyId) {
    try {
      String getQuery = query + param("format", "json") + param("id", spotifyId) + param("bucket", "audio_summary");
      URL url = new URL(baseURL + "/track/profile" + getQuery);
      return get(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  // Get a Track object given a spotifyId from EchoNest.
  public static SongAnalysis getSongMetaData(String spotifyId) {
    String trackJSON = getSongMetaDataJson(spotifyId);
    if(trackJSON == null || trackJSON.isEmpty()) {
      return null;
    } else {
      return gson.fromJson(trackJSON, SongAnalysis.class);
    }
  }

  public static String searchSong(String artist, String song) {
    String getQuery = query
        + param("format", "json")
        + param("artist", artist)
        + param("title", song)
        + param("bucket", "audio_summary")
        + param("results", "1");
    try {
      URL url = new URL(baseURL + "/song/search" + getQuery);
      return get(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  // Get the EchoNest analysis results for a song identified by spotifyId.
  public static String getSongAnalysisJson(String spotifyId) {
    SongAnalysis s = getSongMetaData(spotifyId);
    try {
      URL url = new URL(s.getAnalysisUrl());
      return get(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static SongData getSongAnalysis(String spotifyId) {
    String analysisJSON = getSongAnalysisJson(spotifyId);
    if(analysisJSON == null || analysisJSON.isEmpty()) {
      return null;
    } else {
      return gson.fromJson(analysisJSON, SongData.class);
    }
  }


  // Helper method for neat construction of param arguments.
  private static String param(String key, String val) {
    return String.format("&%s=%s", key, val);
  }
}
