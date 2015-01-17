package uk.co.yojan.kiara.analysis.research;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.PlaylistRequest;
import com.wrapper.spotify.models.Playlist;
import com.wrapper.spotify.models.PlaylistTrack;
import uk.co.yojan.kiara.analysis.tasks.FeatureExtractionTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.SpotifyApi;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class FeatureSelection {

  // A collection of spotify playlist uris to use for supervised
  // feature weight learning for the K-Means clustering.
  private static String spotifyPlaylistIds;

  /*
   * 1. Download each playlist from Spotify's API
   *
   * 2. Mapping Genre --> playlist Id
   *
   *
   */

  public static void loadPlaylist(String playlistURI) throws IOException, WebApiException {
    // e.g. URI spotify:user:spotify:playlist:5yolys8XG4q7YfjYGl5Lff
    String[] uri = playlistURI.split(":");
    String user = uri[2];
    String playlistId = uri[4];

    Logger.getLogger("d").warning(user + " - " + playlistId);

    Api api = SpotifyApi.spotifyApi();
    PlaylistRequest request = api.getPlaylist(user, playlistId).build();
    Playlist playlist = request.get();

    List<PlaylistTrack> tracks = playlist.getTracks().getItems();

    List<Song> songs = new ArrayList<>();

    for(PlaylistTrack track : tracks) {
      Song s = new Song();
      s.setSpotifyId(track.getTrack().getId())
       .setAlbumName(track.getTrack().getAlbum().getName())
       .setArtist(track.getTrack().getArtists().get(0).getName())
       .setSongName(track.getTrack().getName());
      songs.add(s);
    }

    ofy().save().entities(songs).now();
    for(Song song : songs) {
        loadFeatures(song.getArtist(), song.getSongName(), song.getSpotifyId());
    }
  }

  public static void loadFeatures(String artist, String title, String spotifyId) {
    SongAnalysis songAnalysis = ofy().load().key(Key.create(SongAnalysis.class, spotifyId)).now();
    if(songAnalysis == null) {
      songAnalysis = EchoNestApi.getSongAnalysis(spotifyId);
    }
    // As a fallback, search EchoNest with the artist and title name of the song.
    // This often works for obscure or new tracks.
    if(songAnalysis == null) {
      songAnalysis = EchoNestApi.getSongAnalysis(artist, title);
    }

    if(songAnalysis == null) {
      Logger.getLogger("Features").warning("Can't load features for " + artist + " - " + title);
      return;
    }

    songAnalysis.setId(spotifyId);
    SongData songData = ofy().load().key(Key.create(SongData.class, spotifyId)).now();
    if(songData == null) {
      songData = songAnalysis.getSongData();
      songData.setSpotifyId(spotifyId);
    }

    ofy().save().entities(songData, songAnalysis).now();
    TaskManager.featureQueue().add(
        TaskOptions.Builder
            .withPayload(new FeatureExtractionTask(spotifyId))
            .taskName("FeatureExtraction-" + spotifyId + "-" + System.currentTimeMillis()));
  }

  public static void main(String[] args) throws IOException, WebApiException {
    String[] playlistIds = {
        "spotify:user:spotify_uk_:playlist:3nUU3opyeRtDx6Jiyeo7ty",
        "spotify:user:spotify:playlist:3ZgmfR6lsnCwdffZUan8EA",
        "spotify:user:spotify:playlist:7GuiQlzZPwaNHltEXRKQNC",
        "spotify:user:spotify:playlist:445ES7sgFV8zJHebmbUW0L",
        "spotify:user:spotify:playlist:7nPyqcou0SGsteDQQOpySo",
        "spotify:user:121489479:playlist:6Jx7wvYVlwWFANaBDe7qOk",
        "spotify:user:spotify:playlist:2jvJnYj1DfCT1tX0Otp8z7",
        "spotify:user:spotify_uk_:playlist:7ww7zrlarbyDU8LjgYw0O7",
        "spotify:user:spotify_uk_:playlist:5rZjd5MqTPPCtrpX2hvKHt",
        "spotify:user:spotify_uk_:playlist:35iftafjBbC2wWKIgelOf6",
        "spotify:user:spotify:playlist:68zIbo6XemOzCkpkSsc8uD",
        "spotify:user:brianallonce:playlist:6onuJL3dlcxwmBrpcecXeu",
        "spotify:user:spotify:playlist:5yolys8XG4q7YfjYGl5Lff",
        "spotify:user:digster.co.uk:playlist:6fi0ExpvtWx0vQUelJkwmV",
        "spotify:user:spotify:playlist:4jONxQje1Fmw9AFHT7bCp8",
        "spotify:user:arttatus:playlist:1w4ewZUfzwa8eyWkQAf8WY",
        "spotify:user:spotify:playlist:0uwYJZW8x5ArMIUJR4WUZF",
        "spotify:user:spotify:playlist:6YUzc5LbgCeq5NnxKpaN2h",
        "spotify:user:spotify:playlist:2ujjMpFriZ2nayLmrD1Jgl",
        "spotify:user:spotify:playlist:0ExbFrTy6ypLj9YYNMTnmd",
        "spotify:user:spotify:playlist:67nMZWgcUxNa5uaiyLDR2x",
        "spotify:user:spotify:playlist:6R5s2d0D0jHqHKhSfGuif4",
        "spotify:user:spotify:playlist:3fCn2nqmX6ZnYUe9uoty98",
        "spotify:user:spotify:playlist:2Qi8yAzfj1KavAhWz1gaem",
        "spotify:user:spotify_uk_:playlist:6PqXffiom6SgQf6yvmmRHo",
        "spotify:user:spotify:playlist:3dPHWfYO3veKpIpmYKNb2c",
        "spotify:user:spotify:playlist:7jvChGeAMAHMt8QntAEexF",
        "spotify:user:spotify_germany:playlist:0A6TmeTurpzCzp0jTZYSwc",
        "spotify:user:spotify:playlist:04MJzJlzOoy5bTytJwDsVL",
        "spotify:user:spotify:playlist:6iFNvTHtyKvexTwEpEZwl7",
        "spotify:user:spotify_netherlands:playlist:5Vdl8E50GyWL6Ng2vMqhWe",
        "spotify:user:absolutedance:playlist:54XvQQsViMBwjO1ws2o2wx",
        "spotify:user:spotify:playlist:1GQLlzxBxKTb6tJsD4RxHI",
        "spotify:user:spotify_uk_:playlist:6y3CuT7MDDoPNXaD69frug",
        "spotify:user:spotify:playlist:66oi6TpdQHk3kS7ZHOx8gX",
        "spotify:user:spotify_uk_:playlist:3NFZhkxiNzfrUWETRF7rqc",
        "spotify:user:kent1337:playlist:6IjDl5eRczFdgZkKYXhuHZ",
        "spotify:user:spotify:playlist:2dUPFbuNyHAZSNDhLsJ1Hp",
        "spotify:user:spotify_uk_:playlist:2DCf117HtnLpODIYXgqT5r",
        "spotify:user:spotify_uk_:playlist:1h2EsCf8QvKuKiTBzNsdP0",
        "spotify:user:spotify:playlist:6XChIaijnUBzPDrQOX02AJ",
        "spotify:user:spotify:playlist:4bWgWsz9p9eZVtpIvBgbsj",
        "spotify:user:spotify:playlist:7ECmf74Ey57LAYulSxhL9w",
        "spotify:user:spotify:playlist:1atlaFkHkfwnMM8S7jmO6E",
        "spotify:user:spotify:playlist:339IIz2BoOlGAbE7pZ1nJp",
        "spotify:user:spotify:playlist:1wvmrzYMJoHKxf2OSxPpn3",
        "spotify:user:spotify:playlist:0ApdHY8K71F9WrIWbgiI2G",
        "spotify:user:spotify:playlist:1B9o7mER9kfxbmsRH9ko4z",
        "spotify:user:spotify_netherlands:playlist:2cmtcp7GIIhma4sWqLiQnG"
    };
    Api api = SpotifyApi.spotifyApi();
    for(String id : playlistIds) {
      String[] uri = id.split(":");
      String user = uri[2];
      String playlistId = uri[4];
      PrintWriter writer = new PrintWriter(user + "-" + playlistId + ".txt", "UTF-8");
      PlaylistRequest request = api.getPlaylist(user, playlistId).build();
      try{
        Playlist playlist = request.get();
        writer.print("{");
        List<PlaylistTrack> tracks = playlist.getTracks().getItems();
        for(PlaylistTrack t : tracks) writer.print("\"" + t.getTrack().getId() + "\",");
        writer.print("}");
      } catch(Exception e) {
        System.out.println(e.getMessage());
        System.out.println("skipping " + id);
        continue;
      } finally {
        System.out.println("Writing " + id + ".txt");
        writer.close();
      }
    }
  }
}
