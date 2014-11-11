package uk.co.yojan.kiara.android.events;

import com.squareup.okhttp.ResponseBody;
import uk.co.yojan.kiara.client.data.Playlist;

/**
 * Created by yojan on 11/4/14.
 */
public class CreatedPlaylist {

  Playlist p;

  public CreatedPlaylist(Playlist playlist) {
    p = playlist;
  }

  public Playlist getPlaylist() {
    return p;
  }

  public void setPlaylist(Playlist p) {
    this.p = p;
  }
}
