package uk.co.yojan.kiara.analysis.learning.recommendation;


import uk.co.yojan.kiara.server.models.Playlist;

public interface Recommender {

  /**
   *
   * @param userId  the id of the user
   * @param  playlist  the playlist to reccomend next track for
   * @param songId the id of the song to recommend with respect to
   * @return  the spotify id of the song to play next
   */
  public String recommend(String userId, Playlist playlist, String songId);
}
