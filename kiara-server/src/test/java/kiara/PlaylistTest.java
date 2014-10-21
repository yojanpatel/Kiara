package uk.co.yojan.kiara;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.co.yojan.kiara.server.OfyService.ofy;

public class PlaylistTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private User testUser;
  private Key<User> testUserKey;

  private Playlist testPlaylist;
  private Playlist testPlaylist2;
  private Key<Playlist> testPlaylistKey;

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
    testUserKey = Key.create(User.class, "test-user");

    testPlaylist = new Playlist().setName("test-playlist");
    testPlaylist2 = new Playlist().setName("test-platlist-2");
    ofy().save().entities(testPlaylist, testPlaylist2).now();
    testUser.addPlaylist(testPlaylist);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

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
    testPlaylist.addSong(kiara);
    testPlaylist2.addSong(said);

    ArrayList<Song> userSongs = new ArrayList<Song>(testUser.getAllSongs());
    ArrayList<Song> playlistSongs = new ArrayList<Song>(testPlaylist.getAllSongs());
    ArrayList<Song> playlist2Songs = new ArrayList<Song>(testPlaylist2.getAllSongs());

    assertEquals(2, userSongs.size());
    assertEquals(1, playlistSongs.size());
    assertEquals(1, playlist2Songs.size());

    assertEquals("Bonobo", playlistSongs.get(0).getArtist());
    assertEquals("Pearl", playlist2Songs.get(0).getArtist());
  }

  @Test
  public void testRemoveSong() {
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
    testPlaylist.addSong(kiara);
    testPlaylist2.addSong(said);

    testPlaylist.removeSong(kiara.getId()); // Song should still exist in the datastore.

    ArrayList<Song> userSongs = new ArrayList<Song>(testUser.getAllSongs());
    ArrayList<Song> playlistSongs = new ArrayList<Song>(testPlaylist.getAllSongs());
    ArrayList<Song> playlist2Songs = new ArrayList<Song>(testPlaylist2.getAllSongs());

    assertEquals(2, userSongs.size());
    assertEquals(0, playlistSongs.size());
    assertEquals(1, playlist2Songs.size());

    assertNotNull(ofy().load().entity(kiara));
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
    testPlaylist.addSong(kiara);
    testPlaylist.addSong(said);

    Song fetchedKiara = testPlaylist.getSong(kiara.getId());
    Song fetchedSaid = testPlaylist.getSong(said.getId());

    assertEquals("Bonobo", fetchedKiara.getArtist());
    assertEquals("Pearl", fetchedSaid.getArtist());
  }
}
