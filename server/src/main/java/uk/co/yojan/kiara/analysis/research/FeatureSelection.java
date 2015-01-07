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
}
