package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.TrackRequest;
import com.wrapper.spotify.models.Track;
import uk.co.yojan.kiara.server.SpotifyApi;
import uk.co.yojan.kiara.server.serializers.SongSerializer;

import java.util.logging.Logger;

@JsonSerialize(using = SongSerializer.class)
@Entity(name = "Song")
public class Song {

  private static Logger log = Logger.getLogger(Song.class.getName());

  private @Id Long id;
  private String spotifyId;
  private String artistName;
  private String songName;
  private String albumName;
  private String imageURL;


  /* A factory method to create an instance of a Song populated with values
   * pulled from Spotify.
   */
  public static Song newInstanceFromSpotify(String spotifyId) throws Exception{
    Song sm = new Song();
    Api api = SpotifyApi.clientCredentialsApi();
    TrackRequest request = api.getTrack(spotifyId).build();
    Track track = request.get();

    sm.setSpotifyId(spotifyId)
      .setArtist(track.getArtists().get(0).getName())
      .setSongName(track.getName())
      .setAlbumName(track.getAlbum().getName())
      .setImageURL(track.getAlbum().getImages().get(0).getUrl());

    return sm;
  }

  public Long getId() {
    return id;
  }

  public String getSpotifyId() {
    return spotifyId;
  }

  public Song setSpotifyId(String spotifyId) {
    this.spotifyId = spotifyId;
    return this;
  }

  public String getArtist() {
    return artistName;
  }

  public Song setArtist(String artist) {
    this.artistName = artist;
    return this;
  }

  public String getSongName() {
    return songName;
  }

  public Song setSongName(String songName) {
    this.songName = songName;
    return this;
  }

  public String getAlbumName() {
    return albumName;
  }

  public Song setAlbumName(String albumName) {
    this.albumName = albumName;
    return this;
  }

  public String getImageURL() {
    return imageURL;
  }

  public Song setImageURL(String imageURL) {
    this.imageURL = imageURL;
    return this;
  }

  public Song copyFrom(Song from) {
    setSongName(from.getSongName());
    setArtist(from.getArtist());
    setAlbumName(from.getAlbumName());
    setImageURL(from.getImageURL());
    return this;
  }
}
