package uk.co.yojan.kiara.client.data.spotify;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class SearchResult {

  @Expose
  private Albums albums;
  @Expose
  private Artists artists;
  @Expose
  private Tracks tracks;

  public Albums getAlbums() {
    return albums;
  }

  public void setAlbums(Albums albums) {
    this.albums = albums;
  }

  public Artists getArtists() {
    return artists;
  }

  public void setArtists(Artists artists) {
    this.artists = artists;
  }

  public Tracks getTracks() {
    return tracks;
  }

  public void setTracks(Tracks tracks) {
    this.tracks = tracks;
  }

}