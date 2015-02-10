package uk.co.yojan.kiara.server;

/**
 * Secret API constants used by various APIs.
 *
 * Not to be shared.
 */
public class Constants {

  /* Spotify Api Constants. */
  public static final String CLIENT_ID = "efcce40f24e849c0897fd8534515e04d";
  public static final String CLIENT_SECRET = "4babc9987ff74c1dab20a64ad50b29ed";
  public static final String REDIRECT_URI = "kiara://callback";


  public static final String ECHO_NEST_API_KEY = "7Q9SEBKQFSK309IPG";
  public static final String ECHO_NEST_CONSUMER_KEY = "3106153f3b96c42d46cdd4ea8157c5d7";
  public static final String ECHO_NEST_SHARED_SECRET = "tMNaQpdESriv67umPv8Cjg";
  public static final String ECHO_NEST_ANALYSIS_KEY = "7Q9SEBKQFSK309IPG";

  // Playlist History Size
  public static final int HISTORY_SIZE = 50;

  public static final int EVENT_HISTORY_SIZE = 2000;
  public static final int SMALLEST_PLAYLIST_SIZE = 10;

  // public static final double
  // SoftMax Temperature
  public static boolean SOFT_MAX = false;
  public static double SOFTMAX_TEMPERATURE = 2.0;

  // Hypothetical User listening session for experiments
  public static final int LISTENING_SESSION_SIZE = 500;

  // If changes made is  RECLUSTER_FACTOR times bigger than
  // it was the last time it was clustered.
  public static final double RECLUSTER_FACTOR = 0.4;

  // if a song is skipped after completing more than
  // SKIP_SONG_FINISH percent, it is treated as finished
  public static final int SKIP_SONG_FINISH = 80;
}
