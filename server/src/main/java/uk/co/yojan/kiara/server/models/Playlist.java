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


  // Sliding window of the recent listening history
  private int WINDOW_SIZE = 5;
  public LinkedList<String> history;

  public void nowPlaying(String songId) {
    if(history == null) history = new LinkedList<>();
    log.info(history.size() + " size");
    if(history.size() >= WINDOW_SIZE) {
      log.info(history.size() + " window full, removing.");
      history.poll();
    }
    history.add(songId);
    ofy().save().entity(this).now();
  }

  public String previousSong() {
    if(history.size() > 0)
      return history.getLast();
    else return null;
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


}
