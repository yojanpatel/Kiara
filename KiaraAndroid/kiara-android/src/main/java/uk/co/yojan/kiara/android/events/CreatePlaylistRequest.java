package uk.co.yojan.kiara.android.events;

public class CreatePlaylistRequest {

  private String name;

  public CreatePlaylistRequest(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
