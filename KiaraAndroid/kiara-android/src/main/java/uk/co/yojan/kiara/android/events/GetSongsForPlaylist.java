package uk.co.yojan.kiara.android.events;

public class GetSongsForPlaylist {

  private long id;

  public GetSongsForPlaylist(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
