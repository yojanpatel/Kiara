package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import uk.co.yojan.kiara.server.serializers.PlaylistSerializer;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Playlist
 */
@JsonSerialize(using = PlaylistSerializer.class)
@Entity(name = "Playlist")
public class Playlist {
  private static Logger log = Logger.getLogger(Playlist.class.getName());

  @Id private Long id; // Auto-generate.
  private String playlistName;
  private boolean playing;
  private long lastViewedTimestamp;
  private Map<String, Key<Song>> songIdKeyMap = new HashMap<String, Key<Song>>(); // spotifyId ---> Key(spotifyId)


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

  public Collection<Song> getAllSongs() {
    return ofy().load().keys(songIdKeyMap.values()).values();
  }

  public Song getSong(String id) {
    if(songIdKeyMap.containsKey(id))
      return ofy().load().key(songIdKeyMap.get(id)).now();
    else
      return null;
  }

  public Playlist addSong(String spotifyId) {
    songIdKeyMap.put(spotifyId, Key.create(Song.class, spotifyId));
    ofy().save().entity(this).now();
    return this;
  }

  public Playlist addSong(Song song) {
    return addSong(song.getSpotifyId());
  }

  public Playlist removeSong(String songId) {
    songIdKeyMap.remove(songId);
    ofy().save().entity(this).now();
    return this;
  }
}
