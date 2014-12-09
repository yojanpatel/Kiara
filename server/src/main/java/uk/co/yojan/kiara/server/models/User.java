package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.appengine.repackaged.com.google.common.collect.BiMap;
import com.google.appengine.repackaged.com.google.common.collect.HashBiMap;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.UserRequest;
import uk.co.yojan.kiara.server.SpotifyApi;
import uk.co.yojan.kiara.server.serializers.UserSerializer;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/*
 * User - a Google Datastore entity representing a Kiara user.
 */
@Cache
@JsonSerialize(using = UserSerializer.class)
@Entity(name = "User")
public class User {

  private static Logger log = Logger.getLogger(User.class.getName());

  @Id private String id;
  private String facebookId;
  private String firstName;
  private String lastName;
  private String email;
  private String imageURL;

  private HashMap<String, Key<Playlist>> playlistKeyMap = new HashMap<String, Key<Playlist>>();
  private HashMap<String, Key<Song>> songKeyMap = new HashMap<String, Key<Song>>();
//  private HashMap<String, Long> songSpotifyIdMap = new HashMap<>();
  private BiMap<String, Long> songSpotifyIdMap = HashBiMap.create();

  // version for caching.
  private long v;

  public static User newInstanceFromSpotify(String spotifyId) {
    User um = new User();
    Api api = SpotifyApi.clientCredentialsApi();
    UserRequest userRequest = api.getUser(spotifyId).build();
    um.setId(spotifyId);
    try {
      final com.wrapper.spotify.models.User spotifyUser = userRequest.get();
      um.setId(spotifyId)
          .setEmail(spotifyUser.getEmail())
          .setFirstName(spotifyUser.getDisplayName())
          .setImageURL(spotifyUser.getImages().get(0).getUrl());
    } catch (Exception e) {
      log.warning("Could not get User. " + e.getMessage());
    }
    return um;
  }

  public synchronized void incrementCounter() {
    this.v++;
  }

  public String v() {
    return Long.toString(v) + Integer.toString(playlistKeyMap.hashCode())  + Integer.toString(id.hashCode());
  }

  public String getId() {
    return id;
  }

  public User setId(String id) {
    this.id = id;
    return this;
  }

  public String getFacebookId() {
    return facebookId;
  }

  public User setFacebookId(String facebookId) {
    this.facebookId = facebookId;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public User setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public User setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getImageURL() {
    return imageURL;
  }

  public User setImageURL(String imageURL) {
    this.imageURL = imageURL;
    return this;
  }

  public User copyFrom(User from) {
    setFirstName(from.getFirstName());
    setLastName(from.getLastName());
    setFacebookId(from.getFacebookId());
    setEmail(from.getEmail());
    setImageURL(from.getEmail());
    return this;
  }

  public Collection<Playlist> getAllPlaylists() {
    return ofy().load().keys(playlistKeyMap.values()).values();
  }

  public List<PlaylistWithSongs> getPlaylistsWithSongs() {
    ArrayList<Playlist> playlists = new ArrayList<>(getAllPlaylists());

    ArrayList<Map<Key<Song>, Song>> asyncMaps = new ArrayList<>();
    for(Playlist p : playlists) asyncMaps.add(p.getAllSongsAsync());

    List<PlaylistWithSongs> result = new ArrayList<>();
    for(int i = 0; i < playlists.size(); i++) {
      result.add(new PlaylistWithSongs(
          playlists.get(i),
          new ArrayList<Song>(asyncMaps.get(i).values())));
    }
    return result;
  }

  public Playlist getPlaylist(Long id) {
    String idStr = id.toString();
    if(playlistKeyMap.containsKey(idStr)) {
      return ofy().load().key(playlistKeyMap.get(idStr)).now();
    } else {
      return null;
    }
  }

  public boolean hasPlaylist(Long id) {
    String idStr = id.toString();
    return playlistKeyMap.containsKey(idStr);
  }

  public User addPlaylist(Playlist playlist) {
    playlistKeyMap.put(playlist.getId().toString(),
        Key.create(Playlist.class, playlist.getId()));
    ofy().save().entity(this).now();
    return this;
  }


  /*
   * @return true if playlist was successfully deleted.
   */
  public boolean removePlaylist(Long playlistId) {
    if(!hasPlaylist(playlistId))
      return false;

    Result dr = ofy().delete().key(Key.create(Playlist.class, playlistId));
    String idStr = playlistId.toString();
    playlistKeyMap.remove(idStr);
    ofy().save().entity(this).now();
    dr.now();
    return true;
  }

  public Collection<Song> getAllSongs() {
    return ofy().load().keys(songKeyMap.values()).values();
  }

  public Song getSong(Long id) {
    return ofy().load().key(songKeyMap.get(id.toString())).now();
  }

  public Song getSongFromSpotifyId(String spotifyId) {
    return getSong(songSpotifyIdMap.get(spotifyId));
  }

  public boolean hasSong(Long id) {
    String idStr = id.toString();
    return songKeyMap.containsKey(idStr);
  }

  public boolean hasSong(String spotifyId) {
    return songSpotifyIdMap.containsKey(spotifyId);
  }

  /*
  public Song addSong(String spotifyId) throws Exception {
    if(hasSong(spotifyId))
      return null;
    else
      return addSong(Song.newInstanceFromSpotify(spotifyId));
  }*/

  /*
   * Returns null if the user already has the song added,
   * else returns the Song.
   *
  public Song addSong(Song song) {
      ofy().save().entity(song).now();

      String idStr = song.getId().toString();
      songSpotifyIdMap.put(song.getSpotifyId(), song.getId());
      songKeyMap.put(idStr, Key.create(Song.class, song.getId()));
      ofy().save().entity(this).now();
      return song;
  }

  *
  public User removeSong(Long id) {
    if(!hasSong(id))
      return this;

    Result deleteResult = ofy().delete().key(Key.create(Song.class, id));

    String spotifyId = songSpotifyIdMap.inverse().get(id);
    songKeyMap.remove(id.toString());
    songSpotifyIdMap.remove(spotifyId);
    Result saveResult = ofy().save().entity(this);

    Collection<Playlist> playlists = getAllPlaylists();
    for(Playlist p : playlists) {
      p.removeSong(getIdFromSpotifyId(spotifyId));
    }
    ofy().save().entities(playlists); // async
    deleteResult.now();
    saveResult.now();

    return this;
  }*/

  public Long getIdFromSpotifyId(String spotifyId) {
    return songSpotifyIdMap.get(spotifyId);
  }

  /* Spotify --(songSpotifyIdMap)--> Id --(songIdKeyMap)--> Key  */
  public Key<Song> getSongKeyFromSpotifyId(String spotifyId) {
    return songKeyMap.get(songSpotifyIdMap.get(spotifyId).toString());
  }
}
