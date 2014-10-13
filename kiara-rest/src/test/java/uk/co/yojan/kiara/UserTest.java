package uk.co.yojan.kiara;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static uk.co.yojan.kiara.server.OfyService.ofy;

/*
 * Tests regarding the User model.
 * - tests getters and setters.
 * - tests playlist related actions for the user.
 * - tests song related actions for the user.
 */
public class UserTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private User testUser;
  private Key<User> testKey;

  @Before
  public void setUp() {
    helper.setUp();
    testUser = new User()
        .setId("test-user")
        .setFirstName("Test")
        .setLastName("User")
        .setEmail("test-user@kiara.com")
        .setFacebookId("test-user-fb")
        .setImageURL("www.images.com/test");
    testKey = Key.create(User.class, "test-user");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testGetters() {
    assertEquals("test-user", testUser.getId());
    assertEquals("Test", testUser.getFirstName());
    assertEquals("User", testUser.getLastName());
    assertEquals("test-user@kiara.com", testUser.getEmail());
    assertEquals("test-user-fb", testUser.getFacebookId());
    assertEquals("www.images.com/test", testUser.getImageURL());
  }

  @Test
  public void testGetAllPlaylist() {
    Playlist p = new Playlist().setName("test-playlist-1");
    ofy().save().entity(p).now();

    // Tests Id auto-generated.
    assertNotNull(p.getId());
    Long id = p.getId();

    // ADD playlist.
    testUser.addPlaylist(p);

    ArrayList<Playlist> userPlaylists = new ArrayList<Playlist>(testUser.getAllPlaylists());
    assertEquals(1, userPlaylists.size());
    assertEquals(id, userPlaylists.get(0).getId());
    assertEquals("test-playlist-1", userPlaylists.get(0).getName());

    Playlist p2 = new Playlist().setName("test-playlist-2");
    ofy().save().entity(p2).now();
    Long id2 = p2.getId();
    testUser.addPlaylist(p2);

    userPlaylists = new ArrayList<Playlist>(testUser.getAllPlaylists());

    assertEquals(2, userPlaylists.size());

    assertTrue(userPlaylists.get(0).getName().equals("test-playlist-1") ||
        userPlaylists.get(0).getName().equals("test-playlist-2"));

    assertTrue(!userPlaylists.get(1).getName().equals(userPlaylists.get(0).getName()));

    assertTrue(userPlaylists.get(1).getName().equals("test-playlist-1") ||
        userPlaylists.get(1).getName().equals("test-playlist-2"));
  }

  @Test
  public void testGetAllPlaylistFromFetchedUser() {
    Playlist p = new Playlist().setName("test-playlist-1");
    ofy().save().entity(p).now();
    testUser.addPlaylist(p);
    ofy().save().entity(testUser).now();

    Playlist p2 = new Playlist().setName("test-playlist-2");
    ofy().save().entity(p2).now();
    testUser.addPlaylist(p2);
    ofy().save().entity(testUser).now();

    // Fetch User from datastore.
    User fetched = ofy().load().key(Key.create(User.class, "test-user")).now();

    ArrayList<Playlist> userPlaylists = new ArrayList<Playlist>(fetched.getAllPlaylists());

    assertEquals(2, userPlaylists.size());

    assertTrue(userPlaylists.get(0).getName().equals("test-playlist-1") ||
        userPlaylists.get(0).getName().equals("test-playlist-2"));

    assertTrue(!userPlaylists.get(1).getName().equals(userPlaylists.get(0).getName()));

    assertTrue(userPlaylists.get(1).getName().equals("test-playlist-1") ||
        userPlaylists.get(1).getName().equals("test-playlist-2"));
  }

  @Test
  public void testGetPlaylist() {
    Playlist p = new Playlist().setName("test-playlist-1");
    Result<Key<Playlist>> pr = ofy().save().entity(p);
    Playlist p2 = new Playlist().setName("test-playlist-2");
    pr.now();
    ofy().save().entity(p2).now();
    testUser.addPlaylist(p).addPlaylist(p2);

    Long id = p.getId();
    Long id2 = p2.getId();

    // GET playlist.
    Playlist pGet = testUser.getPlaylist(id);
    assertEquals(p.getId(), pGet.getId());
    assertEquals(p.getName(), pGet.getName());
    assertEquals(pGet.getName(), "test-playlist-1");

    Playlist pGet2 = testUser.getPlaylist(id2);
    assertEquals(p2.getId(), pGet2.getId());
    assertEquals(p2.getName(), pGet2.getName());
    assertEquals(pGet2.getName(), "test-playlist-2");
  }

  @Test
  public void testGetPlaylistFromFetchedUser() {
    Playlist p = new Playlist().setName("test-playlist-1");
    Result<Key<Playlist>> pr = ofy().save().entity(p);
    Playlist p2 = new Playlist().setName("test-playlist-2");
    pr.now();
    ofy().save().entity(p2).now();
    testUser.addPlaylist(p).addPlaylist(p2);
    ofy().save().entity(testUser).now();

    Long id = p.getId();
    Long id2 = p2.getId();

    // FETCH USER FROM DATASTORE.
    User fetched = ofy().load().key(Key.create(User.class, "test-user")).now();

    Playlist pGet = fetched.getPlaylist(id);
    assertEquals(p.getId(), pGet.getId());
    assertEquals(p.getName(), pGet.getName());
    assertEquals(pGet.getName(), "test-playlist-1");

    Playlist pGet2 = fetched.getPlaylist(id2);
    assertEquals(p2.getId(), pGet2.getId());
    assertEquals(p2.getName(), pGet2.getName());
    assertEquals(pGet2.getName(), "test-playlist-2");
  }

  @Test
  public void testRemovePlaylist() {
    Playlist p = new Playlist().setName("test-playlist-1");
    Result<Key<Playlist>> pr = ofy().save().entity(p);
    Playlist p2 = new Playlist().setName("test-playlist-2");
    pr.now();
    ofy().save().entity(p2).now();
    testUser.addPlaylist(p).addPlaylist(p2);

    Long id = p.getId();
    Long id2 = p2.getId();

    testUser.removePlaylist(id);
    ArrayList<Playlist> userPlaylists = new ArrayList<Playlist>(testUser.getAllPlaylists());
    assertEquals(1, userPlaylists.size());
    assertEquals(id2, userPlaylists.get(0).getId());
    assertEquals("test-playlist-2", userPlaylists.get(0).getName());
  }

  @Test
  public void testRemovePlaylistFromFetchedUser() {
    Playlist p = new Playlist().setName("test-playlist-1");
    Result<Key<Playlist>> pr = ofy().save().entity(p);
    Playlist p2 = new Playlist().setName("test-playlist-2");
    pr.now();
    ofy().save().entity(p2).now();
    testUser.addPlaylist(p).addPlaylist(p2);
    ofy().save().entity(testUser).now();

    Long id = p.getId();
    Long id2 = p2.getId();

    testUser = ofy().load().key(Key.create(User.class, "test-user")).now();
    testUser.removePlaylist(id);
    ofy().save().entity(testUser).now();

    assertNull(ofy().load().entity(p).now());

    User fetched = ofy().load().key(Key.create(User.class, "test-user")).now();
    ArrayList<Playlist> userPlaylists = new ArrayList<Playlist>(fetched.getAllPlaylists());
    assertEquals(1, userPlaylists.size());
    assertEquals(id2, userPlaylists.get(0).getId());
    assertEquals("test-playlist-2", userPlaylists.get(0).getName());
  }


  // Songs
  @Test
  public void testGetAllSongs() {
    Song kiara = new Song()
        .setSpotifyId("7sqii6BhIDpJChYpU3WjwS")
        .setArtist("Bonobo")
        .setAlbumName("Black Sands")
        .setSongName("Kiara");
    Song said = new Song()
        .setSpotifyId("1SCr765E8UhorcLL6UDKjf")
        .setArtist("Pearl")
        .setAlbumName("Open")
        .setSongName("Said");

    testUser.addSong(kiara);
    testUser.addSong(said);

    try {
      // Adding the same song (Bonobo - Kiara) should return null.
      assertNull(testUser.addSong("7sqii6BhIDpJChYpU3WjwS"));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    User fetched = ofy().load().key(testKey).now();

    ArrayList<Song> userSongs = new ArrayList<Song>(fetched.getAllSongs());

    assertEquals(2, userSongs.size());

    assertTrue(userSongs.get(0).getArtist().equals(kiara.getArtist()) ||
        userSongs.get(0).getArtist().equals(said.getArtist()));

    assertTrue(!userSongs.get(1).getArtist().equals(userSongs.get(0).getArtist()));

    assertTrue(userSongs.get(1).getArtist().equals(kiara.getArtist()) ||
        userSongs.get(1).getArtist().equals(said.getArtist()));
  }

  @Test
  public void testGetSong() {
    Song kiara = new Song()
        .setSpotifyId("7sqii6BhIDpJChYpU3WjwS")
        .setArtist("Bonobo")
        .setAlbumName("Black Sands")
        .setSongName("Kiara");
    Song said = new Song()
        .setSpotifyId("1SCr765E8UhorcLL6UDKjf")
        .setArtist("Pearl")
        .setAlbumName("Open")
        .setSongName("Said");

    testUser.addSong(kiara);
    testUser.addSong(said);

    User fetched = ofy().load().key(testKey).now();

    assertEquals(fetched.getSong(kiara.getId()).getArtist(), kiara.getArtist());
    assertEquals(fetched.getSongFromSpotifyId("1SCr765E8UhorcLL6UDKjf").getArtist(), said.getArtist());
  }

  @Test
  public void testDeleteSong() {
    Song kiara = new Song()
        .setSpotifyId("7sqii6BhIDpJChYpU3WjwS")
        .setArtist("Bonobo")
        .setAlbumName("Black Sands")
        .setSongName("Kiara");
    Song said = new Song()
        .setSpotifyId("1SCr765E8UhorcLL6UDKjf")
        .setArtist("Pearl")
        .setAlbumName("Open")
        .setSongName("Said");

    testUser.addSong(kiara);
    testUser.addSong(said);
    assertNotNull(ofy().load().entity(kiara).now());

    User fetched = ofy().load().key(testKey).now();
    assertEquals(2, fetched.getAllSongs().size());

    fetched.removeSong(kiara.getId()); // remove Kiara song.
    fetched.removeSong(-1L); // no effect.

    ArrayList<Song> userSongs = new ArrayList<Song>(fetched.getAllSongs());
    assertEquals(1, userSongs.size());
    assertEquals("Pearl", userSongs.get(0).getArtist());

    assertNull(ofy().load().entity(kiara).now());
  }
}