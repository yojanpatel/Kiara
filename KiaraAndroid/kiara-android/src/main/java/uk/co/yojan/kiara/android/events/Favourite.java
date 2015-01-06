package uk.co.yojan.kiara.android.events;

/**
 * Created by yojan on 12/25/14.
 */
public class Favourite {

  boolean favourited;

  public Favourite(boolean favourited) {
    this.favourited = favourited;
  }

  public boolean isFavourited() {
    return favourited;
  }

  public void setFavourited(boolean favourited) {
    this.favourited = favourited;
  }
}
