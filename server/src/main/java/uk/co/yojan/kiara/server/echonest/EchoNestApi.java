package uk.co.yojan.kiara.server.echonest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.yojan.kiara.server.ApiConstants;
import uk.co.yojan.kiara.server.models.SongData;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.serializers.SongDataDeserializer;
import uk.co.yojan.kiara.server.serializers.SongMetaDataDeserializer;
import uk.co.yojan.kiara.server.serializers.SongSearchDeserializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


public class EchoNestApi {

  private static Logger log = Logger.getLogger("EchoNestApi");

  private static String baseURL = "http://developer.echonest.com/api/v4";
  private static String query = "?api_key=" + ApiConstants.ECHO_NEST_API_KEY;

  private static Gson gson = new GsonBuilder()
      .registerTypeAdapter(SongAnalysis.class, new SongMetaDataDeserializer())
      .registerTypeAdapter(SongData.class, new SongDataDeserializer())
      .create();

  private static Gson searchGson = new GsonBuilder()
      .registerTypeAdapter(SongAnalysis.class, new SongSearchDeserializer())
      .create();

  public static String get2(URL url) {
    try {
      HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      urlConn.setRequestMethod("GET");
      urlConn.setReadTimeout(10000); // 10 seconds
      InputStream is = urlConn.getInputStream();
      byte[] bytes = new byte[is.available()];
      is.read(bytes, 0, is.available());
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String get(URL url) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
      log.info(sb.toString());
      return sb.toString();
    } catch (MalformedURLException e) {
      log.warning(e.toString());
    } catch (IOException e) {
      log.warning(e.toString());
    }
    return null;
  }

  // Get the raw JSON content from an EchoNest request given a spotifyId. (sync)
  private static String getSongMetaDataJson(String spotifyId) {
    try {
      String getQuery = query +
          param("format", "json") +
          param("id", songUri(spotifyId)) +
          param("bucket", "audio_summary");
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

  public static String searchSongJson(String artist, String song) {
    String getQuery = null;
    try {
      getQuery = query
          + param("format", "json")
          + param("artist", URLEncoder.encode(artist, StandardCharsets.UTF_8.toString()))
          + param("title", URLEncoder.encode(song, StandardCharsets.UTF_8.toString()))
          + param("bucket", "audio_summary")
          + param("results", "1");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    try {
      URL url = new URL(baseURL + "/song/search" + getQuery);
      return get(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static SongAnalysis searchSong(String artist, String song) {
    String analysisJSON = searchSongJson(artist, song);
    if(analysisJSON == null || analysisJSON.isEmpty()) {
      return null;
    } else {
      return searchGson.fromJson(analysisJSON, SongAnalysis.class);
    }
  }

  // Get the EchoNest analysis results for a song identified by spotifyId.
  public static String getSongDataJson(SongAnalysis analysis) {
    try {
      URL url = new URL(analysis.getAnalysisUrl());
      return get(url);
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static SongData getSongData(SongAnalysis songAnalysis) {
    String analysisJSON = getSongDataJson(songAnalysis);
    if(analysisJSON == null || analysisJSON.isEmpty()) {
      return null;
    } else {
      return gson.fromJson(analysisJSON, SongData.class);
    }
  }

  public static SongAnalysis getSongAnalysis(String spotifyId) {
    SongAnalysis songAnalysis = getSongMetaData(spotifyId);

    if(songAnalysis == null)
      return null;

    SongData songData = getSongData(songAnalysis);
    songAnalysis.setSongData(songData);
    return songAnalysis;
  }

  public static SongAnalysis getSongAnalysis(String artist, String title) {
    SongAnalysis songAnalysis = searchSong(artist, title);

    if(songAnalysis == null)
      return null;

    SongData songData = getSongData(songAnalysis);
    songAnalysis.setSongData(songData);
    return songAnalysis;
  }

  private static String songUri(String spotifyId) {
    return "spotify:track:" + spotifyId;
  }

  // Helper method for neat construction of param arguments.
  private static String param(String key, String val) {
    return String.format("&%s=%s", key, val);
  }

  public static void main(String[] args) {
    String json = getSongMetaDataJson("4Gkd4msFAFTiDxFZcg9r8i");
    System.out.println(json);
    SongAnalysis s = searchSong("Emika", "Filters");
    System.out.println(s.getArtist());
  }
}
