package uk.co.yojan.kiara.client.data.spotify;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

  @Expose private Tracks tracks;
  @Expose private List<Image> images = new ArrayList<Image>();

  private Tracks getTracks() {
    return tracks;
  }

  private void setTracks(Tracks tracks) {
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


  public class Tracks {

    @Expose
    private List<Item> items = new ArrayList<Item>();

    public List<Item> getItems() {
      return items;
    }
    public void setItems(List<Item> items) {
      this.items = items;
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
}