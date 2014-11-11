package uk.co.yojan.kiara.android.events;

public class SearchRequest {

  private String query;
  private int offset;
  private int limit;
  private long playlistId;

  public SearchRequest(String query, long playlistId, int offset, int limit) {
    this.query = query;
    this.playlistId = playlistId;
    this.offset = offset;
    this.limit = limit;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }
}
