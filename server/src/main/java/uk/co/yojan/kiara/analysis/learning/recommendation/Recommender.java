package uk.co.yojan.kiara.analysis.learning.recommendation;


public interface Recommender {

  /**
   *
   * @param userId  the id of the user
   * @param  playlistId  the id of the playlist to reccomend next track for
   * @return  the spotify id of the song to play next
   */
  public String recommend(String userId, Long playlistId);
}
