package uk.co.yojan.kiara.client.data;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Playlist {

  @Expose
  private Long id;
  @Expose
  @SerializedName("name")
  private String playlistName;
  @Expose
  private Long lastViewedTimestamp;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPlaylistName() {
    return playlistName;
  }

  public void setPlaylistName(String playlistName) {
    this.playlistName = playlistName;
  }

  public Long getLastViewedTimestamp() {
    return lastViewedTimestamp;
  }

  public void setLastViewedTimestamp(Long lastViewedTimestamp) {
    this.lastViewedTimestamp = lastViewedTimestamp;
  }
}