package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import uk.co.yojan.kiara.server.Constants;
import uk.co.yojan.kiara.server.serializers.PlaylistDeserializer;
import uk.co.yojan.kiara.server.serializers.PlaylistSerializer;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Playlist
 */
@JsonSerialize(using = PlaylistSerializer.class)
@JsonDeserialize(using = PlaylistDeserializer.class)
@Entity(name = "Playlist")
public class Playlist {

  private static Logger log = Logger.getLogger(Playlist.class.getName());

  @Id private Long id; // Auto-generate.
  private String playlistName;
  private boolean playing;
  private long lastViewedTimestamp;
  private Map<String, Key<Song>> songIdKeyMap = new HashMap<String, Key<Song>>(); // spotifyId ---> Key(spotifyId)
//  @Serialize(zip = true)
//  private ArrayList<Double> weights;



  // version for caching
  private long v;

  private int lastClusterSize;
  private int changesSinceLastCluster;

  private boolean clusterReady;
  private boolean relearning;

  // A sliding window of the songs played recently.
  private LinkedList<String> history = new LinkedList<>();

  // last successful song and timestamp
  private String lastFinished;
  private long timestamp;

  // A sliding window of the user's events caused affected the learning algorithms.
  // also allows training of Q based on different reward functions/strategies
  private LinkedList<String> events = new LinkedList<>();

//  private ArrayList<String> similarSongs;

  private int historySize() {
    return (int) Math.sqrt(songIdKeyMap.size());
  }

  public void nowPlaying(String songId) {
    if(history == null) history = new LinkedList<>();
    if(history.size() >= historySize()) {
      history.poll();
    }
    history.add(songId);
  }

  public void justFinished(String songId) {
    lastFinished = songId;
    timestamp = System.currentTimeMillis();
  }

  // Should return the id of the last successfully completed song.
  public String previousSong() {
    if(history == null) {
      history = new LinkedList<>();
    }
    if(history.size() > 0)
      return history.getLast();
    else return null;
  }

  public String lastFinished() {
    // 1 hour threshold, if last song was played over an hour ago
    if(timestamp < System.currentTimeMillis() - (60 * 60 *  1000)) {
      Logger.getLogger("").warning("Previous song was played more than an hour ago, starting fresh. " + timestamp  + " " + " " + System.currentTimeMillis() + " " + (System.currentTimeMillis() - (60 * 60 * 1000)));
      return null;
    }
    return lastFinished;
  }


  public synchronized void incrementCounter() {
    this.v++;
  }

  public String v() {
    return id.toString() + Integer.toString(songIdKeyMap.hashCode()) + Integer.toString(playlistName.hashCode());
  }

  public Long getId() {
    return id;
  }

  public Playlist setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return playlistName;
  }

  public Playlist setName(String playlistName) {
    this.playlistName = playlistName;
    return this;
  }

  public boolean getPlaying() {
    return playing;
  }

  public Playlist setPlaying() {
    this.playing = true;
    return this;
  }

  public Playlist setNotPlaying() {
    this.playing = false;
    return this;
  }

  public long getLastViewedTimestamp() {
    return lastViewedTimestamp;
  }

  public Playlist updateLastViewed() {
    lastViewedTimestamp = new Date().getTime();
    return this;
  }

  public Playlist copyFrom(Playlist from) {
    setName(from.getName());
    return this;
  }

  public Collection<String> getAllSongIds() {
    return songIdKeyMap.keySet();
  }

  public Collection<Song> getAllSongs() {
    return ofy().load().keys(songIdKeyMap.values()).values();
  }

  public Map<Key<Song>, Song> getAllSongsAsync() {
    return ofy().load().keys(songIdKeyMap.values());
  }

  public Song getSong(String id) {
    if(songIdKeyMap.containsKey(id))
      return ofy().load().key(songIdKeyMap.get(id)).now();
    else
      return null;
  }

  public Result addSong(String spotifyId) {
    songIdKeyMap.put(spotifyId, Key.create(Song.class, spotifyId));
    incrementCounter();
    changesSinceLastCluster++;
    return ofy().save().entity(this);
  }

  public Result addSong(Song song) {
    changesSinceLastCluster++;
    return addSong(song.getSpotifyId());
  }

  public Result addSongs(String... ids) {
    for(String id : ids) {
      songIdKeyMap.put(id, Key.create(Song.class, id));
    }

    // add additional constraints to help recluster
//    for(int i = 0; i < ids.length - 1; i++) {
//      addSimilarSong(ids[i], ids[i+1]);
//    }

    incrementCounter();
    changesSinceLastCluster += ids.length;
    return ofy().save().entity(this);
  }

  public Result addSongs(Song... songs) {
    ArrayList<Song> ss = new ArrayList<Song>();
    for (Song s : songs) {
      ss.add(s);
    }

    // add additional constraints to help recluster
//    for(int i = 0; i < songs.length - 1; i++) {
//      addSimilarSong(songs[i].getSpotifyId(), songs[i+1].getSpotifyId());
//    }

    changesSinceLastCluster += songs.length;
    return addSongs(ss);
  }

  public Result addSongs(List<Song> songs) {
    for(Song s : songs) {
      songIdKeyMap.put(s.getId(), Key.create(Song.class, s.getId()));
    }

    // add additional constraints to help recluster
//    for(int i = 0; i < songs.size() - 1; i++) {
//      addSimilarSong(songs.get(i).getSpotifyId(), songs.get(i+1).getSpotifyId());
//    }

    changesSinceLastCluster += songs.size();
    incrementCounter();
    return ofy().save().entity(this);
  }

  public Result removeSong(String songId) {
    songIdKeyMap.remove(songId);
    changesSinceLastCluster++;
    incrementCounter();
    return ofy().save().entity(this);
  }

  public boolean needToRecluster() {
    if(getAllSongIds().size() < Constants.SMALLEST_PLAYLIST_SIZE) {
      return false;
    }
    return changesSinceLastCluster > Constants.RECLUSTER_FACTOR * getAllSongIds().size();
  }

  public LinkedList<String> history() {
    if(history == null) history = new LinkedList<>();
    return history;
  }

  public void setHistory(LinkedList<String> history) {
    this.history = history;
  }

  public LinkedList<String> events() {
    if(events == null) events = new LinkedList<>();
    return events;
  }

  public void setEvents(LinkedList<String> events) {
    this.events = events;
  }

  public void clearHistory() {
    history = new LinkedList<>();
    events = new LinkedList<>();
  }

  public int getLastClusterSize() {
    return lastClusterSize;
  }

  public void setLastClusterSize(int lastClusterSize) {
    this.lastClusterSize = lastClusterSize;
  }

  public int getChangesSinceLastCluster() {
    return changesSinceLastCluster;
  }

  public void setChangesSinceLastCluster(int changesSinceLastCluster) {
    this.changesSinceLastCluster = changesSinceLastCluster;
  }

  public boolean isClusterReady() {
    return clusterReady;
  }

  public void setClusterReady(boolean clusterReady) {
    this.clusterReady = clusterReady;
  }

  public boolean useCluster() {
    return size() > Constants.SMALLEST_PLAYLIST_SIZE && isClusterReady();
  }

  public boolean isRelearning() {
    return relearning;
  }

  public void setRelearning(boolean relearning) {
    this.relearning = relearning;
  }

//  public ArrayList<Double> getWeights() {
//    return weights;
//  }
//
//  public void setWeights(ArrayList<Double> weights) {
//    this.weights = weights;
//  }

  public int size() {
    return getAllSongIds().size();
  }

//  public void addSimilarSong(String id1, String id2) {
//    if(similarSongs == null) similarSongs = new ArrayList<>();
//    similarSongs.add(id1 + "-" + id2);
//  }
//
//  public ArrayList<SimilarPair> getSimilarSongs() {
//    if(similarSongs == null) similarSongs = new ArrayList<>();
//    ArrayList<SimilarPair> similar = new ArrayList<>();
//    for(String s : similarSongs) {
//      String[] parts = s.split("-");
//      similar.add(new SimilarPair(parts[0], parts[1]));
//    }
//    return similar;
//  }
}
