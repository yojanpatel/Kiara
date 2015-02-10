package uk.co.yojan.kiara.analysis.features;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.TrackRequest;
import com.wrapper.spotify.models.Track;
import uk.co.yojan.kiara.server.SpotifyApi;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadPreviews {

  public static void main(String[] args) throws IOException, WebApiException {


    Api api = SpotifyApi.clientCredentialsApi();

    String playlistId = "chill0";

    for(String id : args) {
      System.out.println("Downloading " + id);
      TrackRequest request = api.getTrack(id).build();
      Track track = request.get();
      track.getPreviewUrl();
      System.out.println(track.getPreviewUrl());
      try {
        URLConnection conn = new URL(track.getPreviewUrl()).openConnection();
        InputStream is = conn.getInputStream();

        OutputStream outstream = new FileOutputStream(new File("previews/" + playlistId + "-" + track.getId() + ".mp3"));
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0) {
          outstream.write(buffer, 0, len);
        }
        outstream.close();
      } catch(IOException e) {
        e.printStackTrace();
      }

    }
  }
}