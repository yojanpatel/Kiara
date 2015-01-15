package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
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

  // version for caching
  private long v;


  // A sliding window of the songs played recently.
  private LinkedList<String> history;

  // last successful song and timestamp
  private String lastFinished;
  private long timestamp;

  // A sliding window of the user's events caused affected the learning algorithms.
  // also allows training of Q based on different reward functions/strategies
  private LinkedList<String> events;

  public void nowPlaying(String songId) {
    if(history == null) history = new LinkedList<>();
    int WINDOW_SIZE = 50;
    if(history.size() >= WINDOW_SIZE) {
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
    if(history.size() > 0)
      return history.getLast();
    else return null;
  }

  public String lastFinished() {
    // 1 hour threshold, if last song was played over an hour ago
    if(timestamp < System.currentTimeMillis() - (60 * 1000)) {
      Logger.getLogger("").warning("Previous song was played more than an hour ago, starting fresh. " + timestamp  + " " + (System.currentTimeMillis() - (60 * 1000)));
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
    return ofy().save().entity(this);
  }

  public Result addSong(Song song) {
    return addSong(song.getSpotifyId());
  }

  public Result addSongs(String... ids) {
    for(String id : ids) {
      songIdKeyMap.put(id, Key.create(Song.class, id));
    }
    incrementCounter();
    return ofy().save().entity(this);
  }

  public Result addSongs(Song... songs) {
    ArrayList<Song> ss = new ArrayList<Song>();
    for(Song s : songs) ss.add(s);
    return addSongs(ss);
  }

  public Result addSongs(List<Song> songs) {
    for(Song s : songs) {
      songIdKeyMap.put(s.getId(), Key.create(Song.class, s.getId()));
    }
    incrementCounter();
    return ofy().save().entity(this);
  }

  public Playlist removeSong(String songId) {
    songIdKeyMap.remove(songId);
    incrementCounter();
    ofy().save().entity(this).now();
    return this;
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
  }
}
