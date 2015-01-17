package uk.co.yojan.kiara.analysis.learning;

/**
 * Encapsulates an event sent by the user signifying some actions have been taken
 * and should be learnt from etc.
 */
public class ActionEvent {

  // represents a transition from one song to another
  String previousSongId;
  boolean skipped;
  int percentage;

  boolean favourited;

  String startedSongId;

  public boolean isSkipped() {
    return skipped;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
  }

  public void setPercentage(int percentage) {
    this.percentage = percentage;
  }

  public int getPercentage() {
    if(skipped) {
      return percentage;
    } else {
      return 100;
    }
  }

  public String getEndedSongId() {
    return previousSongId;
  }

  public void setPreviousSongId(String previousSongId) {
    this.previousSongId = previousSongId;
  }

  public String getStartedSongId() {
    return startedSongId;
  }

  public void setStartedSongId(String startedSongId) {
    this.startedSongId = startedSongId;
  }

  public String getPreviousSongId() {
    return previousSongId;
  }

  public boolean isFavourited() {
    return favourited;
  }

  public void setFavourited(boolean favourited) {
    this.favourited = favourited;
  }
}
