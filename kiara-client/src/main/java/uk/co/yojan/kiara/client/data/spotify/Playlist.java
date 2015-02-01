package uk.co.yojan.kiara.client.data.spotify;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

  @Expose private String name;
  @Expose private Owner owner;
  @Expose private String uri;
  @Expose private PlaylistTracks tracks;
  @Expose private List<Image> images = new ArrayList<Image>();

  public PlaylistTracks getTracks() {
    return tracks;
  }

  private void setTracks(PlaylistTracks tracks) {
    this.tracks = tracks;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }


  public String getImageUrl() {
    if(getImages().size() > 0) {
      return getImages().get(0).getUrl();
    }
    return "";
  }

  public Track getTrack(int pos) {
    return getTracks().getItems().get(pos).getTrack();
  }

  public List<Track> getTrackList() {
    List<Track> tracks = new ArrayList<Track>();
    List<Item> items = getTracks().getItems();
    for(Item i : items) {
      tracks.add(i.getTrack());
    }
    return tracks;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Owner getOwner() {
    return owner;
  }

  public void setOwner(Owner owner) {
    this.owner = owner;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public class PlaylistTracks {

    @Expose private List<Item> items = new ArrayList<Item>();
    @Expose private int total;

    public List<Item> getItems() {
      return items;
    }
    public void setItems(List<Item> items) {
      this.items = items;
    }

    public int getTotal() {
      return total;
    }

    public void setTotal(int total) {
      this.total = total;
    }
  }

  public class Item {

    @Expose
    private Track track;

    public Track getTrack() {
      return track;
    }

    public void setTrack(Track track) {
      this.track = track;
    }

  }

  public class Owner {
    @Expose String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }
}