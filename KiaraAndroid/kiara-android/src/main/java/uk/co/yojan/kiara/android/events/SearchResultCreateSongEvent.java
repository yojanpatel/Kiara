package uk.co.yojan.kiara.android.events;

import uk.co.yojan.kiara.client.data.spotify.Albums;
import uk.co.yojan.kiara.client.data.spotify.Artists;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.Tracks;

/**
 * Decorator for SearchResult to also contain the playlist that invoked.
 */
public class SearchResultCreateSongEvent extends SearchResult {

  private long playlistId;
  private SearchResult result;

  public SearchResultCreateSongEvent(long playlistId, SearchResult result) {
    this.playlistId = playlistId;
    this.result = result;
  }

  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }

  public SearchResult getResult() {
    return result;
  }

  public void setResult(SearchResult result) {
    this.result = result;
  }

  @Override
  public Albums getAlbums() {
    return result.getAlbums();
  }

  @Override
  public Artists getArtists() {
    return result.getArtists();
  }

  @Override
  public Tracks getTracks() {
    return result.getTracks();
  }
}
