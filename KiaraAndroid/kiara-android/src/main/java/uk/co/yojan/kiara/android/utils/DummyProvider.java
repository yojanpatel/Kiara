package uk.co.yojan.kiara.android.utils;

import uk.co.yojan.kiara.client.data.Playlist;

import java.util.ArrayList;
import java.util.List;

public class DummyProvider {
  public static List<Playlist> getPlaylists() {
    ArrayList<Playlist> dummy = new ArrayList<Playlist>();

    Playlist p = new Playlist();
    p.setPlaylistName("NorCal");

    Playlist q = new Playlist();
    q.setPlaylistName("SoCal");

    dummy.add(p);
    dummy.add(q);

    return dummy;
  }
}
