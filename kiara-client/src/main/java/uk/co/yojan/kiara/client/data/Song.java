package uk.co.yojan.kiara.client.data;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Song {

  @Expose
  private Long id;
  @Expose
  private String spotifyId;
  @Expose
  private String songName;
  @Expose
  private String artistName;
  @Expose
  private String albumName;
  @Expose
  private String imageURL;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSpotifyId() {
    return spotifyId;
  }

  public void setSpotifyId(String spotifyId) {
    this.spotifyId = spotifyId;
  }

  public String getSongName() {
    return songName;
  }

  public void setSongName(String songName) {
    this.songName = songName;
  }

  public String getArtistName() {
    return artistName;
  }

  public void setArtistName(String artistName) {
    this.artistName = artistName;
  }

  public String getAlbumName() {
    return albumName;
  }

  public void setAlbumName(String albumName) {
    this.albumName = albumName;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

}