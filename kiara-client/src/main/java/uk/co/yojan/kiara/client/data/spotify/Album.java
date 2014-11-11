package uk.co.yojan.kiara.client.data.spotify;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Album {

  @SerializedName("album_type")
  @Expose
  private String albumType;
  @SerializedName("artists")
  @Expose
  private Artists artists;
  @SerializedName("available_markets")
  @Expose
  private List<String> availableMarkets = new ArrayList<String>();
  @Expose
  private String href;
  @Expose
  private String id;
  @Expose
  private List<Image> images = new ArrayList<Image>();
  @Expose
  private String name;
  @Expose
  private String type;
  @Expose
  private String uri;

  public String getAlbumType() {
    return albumType;
  }

  public void setAlbumType(String albumType) {
    this.albumType = albumType;
  }

  public List<String> getAvailableMarkets() {
    return availableMarkets;
  }

  public void setAvailableMarkets(List<String> availableMarkets) {
    this.availableMarkets = availableMarkets;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Artists getArtists() {
    return artists;
  }

  public void setArtists(Artists artists) {
    this.artists = artists;
  }
}