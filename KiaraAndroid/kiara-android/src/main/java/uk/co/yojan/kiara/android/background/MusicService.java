package uk.co.yojan.kiara.android.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.*;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.*;
import uk.co.yojan.kiara.android.activities.PlayerActivity;
import uk.co.yojan.kiara.android.events.Favourite;
import uk.co.yojan.kiara.android.events.PlaybackEvent;
import uk.co.yojan.kiara.android.events.QueueSongRequest;
import uk.co.yojan.kiara.android.events.SeekbarProgressChanged;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

import java.util.LinkedList;

/**
 * Background, long-running service that handles the Spotify streaming and
 * callbacks to the UI threads.
 */
public class MusicService extends Service
    implements PlayerNotificationCallback, PlayerStateCallback, AudioManager.OnAudioFocusChangeListener {

  private static final String log = MusicService.class.getCanonicalName();

  private KiaraApplication application;

  private KiaraPlayer player;
  public enum RepeatState {FALSE, ONE, TRUE}
  private RepeatState repeating;
  private boolean playing;
  private boolean favourited;


  private long playlistId;
  private Song currentSong;
  private int duration;
  private int position;
  private int lastSkip;
  private Bitmap currentSongAlbumCover;

  public LinkedList<Song> playQueue;

  // Locks that have to be acquired from Android if streaming is taking place.
  private PowerManager.WakeLock wakeLock;
  private WifiManager.WifiLock wifiLock;

  private NotificationManager mNotificationManager;


  private Handler mHandler = new Handler();
  private Runnable mRunnable;

  // Binder given to clients.
  private final IBinder mBinder = new MusicBinder();

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(log, "onBind");
    return mBinder;
  }

  @Override
  public void onCreate() {
    Log.d(log, "onCreate");
    initialise();
  }

  /**
   * Called every time a new intent is sent to this service.
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(log, "onStartCommand");

    if(intent.getBooleanExtra(Constants.PLAY_ACTION, false)) {
      String spotifyUri = intent.getStringExtra(Constants.SONG_URI);
      Log.d(log, "Play action received " + spotifyUri);

    } else if(intent.getAction() != null) {
      if (intent.getAction().equals(Constants.ACTION_STOP_SERVICE)) {
        Log.d(log, "Stopping service from intent.");
        stopSelf();

      } else if (intent.getAction().equals(Constants.ACTION_PLAY_PAUSE)) {
        Log.d(log, "play/pause from intent.");
        pauseplay();
      } else if (intent.getAction().equals(Constants.ACTION_FAVOURITE)) {
        Log.d(log, "fav from intent");
        toggleFav();
      }
    }
    return START_STICKY;
  }

  private void initialise() {
    Log.d(log, "initialising Music Service");
    application = (KiaraApplication)getApplication();
    application.getBus().register(this);

    initialiseHandlers();

    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLockTag");
    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
        .createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLockTag");

    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    playQueue = new LinkedList<Song>();

    initialisePlayer();
  }

  /**
   * Called by the system to notify a Service that it is no longer used and is being removed.  The
   * service should clean up any resources it holds (threads, registered
   * receivers, etc) at this point.  Upon return, there will be no more calls
   * in to this Service object and it is effectively dead.  Do not call this method directly.
   */
  @Override
  public void onDestroy() {
    Log.d(log, "onDestroy");
    application.getBus().unregister(this);
    Spotify.destroyPlayer(this);
    sharedPreferences().edit().putBoolean(Constants.IN_SESSION, false).commit();
    super.onDestroy();
  }

  private EncryptedSharedPreferences sharedPreferences() {
    return ((KiaraApplication)getApplication()).sharedPreferences();
  }

  private void initialisePlayer() {
    String accessToken = sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
    if(accessToken != null) {

      Spotify spotify = new Spotify();
      Config playerConfig = new Config(this, accessToken, Constants.CLIENT_ID);

      KiaraPlayer kplayer = KiaraPlayer.create(playerConfig, new Player.InitializationObserver() {

        @Override
        public void onInitialized() {
          Log.d(log, "OnInitialized");
          player.addPlayerNotificationCallback(MusicService.this);
        }

        @Override
        public void onError(Throwable throwable) {
          Log.d(log, "onError");
        }
      });

      spotify.setPlayer(kplayer);
      player = (KiaraPlayer) spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {

        @Override
        public void onInitialized() {
          Log.d(log, "OnInitialized");
          player.addPlayerNotificationCallback(MusicService.this);
        }

        @Override
        public void onError(Throwable throwable) {
          Log.d(log, "onError");
        }
      });
    }
  }

  @Override
  public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

    // TRACK_START: called every time a new track starts playing
    if (eventType == EventType.TRACK_START) {
      Log.d("KiaraPlayerEvent", "Track started " + playerState.trackUri);
      application.learningApi().trackStarted(
          application.userId(),
          playlistId,
          stripSpotifyUri(playerState.trackUri),
          updateOnCallback);

      if (repeating == RepeatState.ONE) {
        player.setRepeat(false);
        repeating = RepeatState.FALSE;
      }

    // END_OF_CONTEXT: end of a song that finished playing
    } else if (eventType == EventType.END_OF_CONTEXT) {
      Log.d("KiaraPlayerEvent", "Track ended " + playerState.trackUri);
      application.learningApi().trackFinished(
          application.userId(),
          playlistId,
          stripSpotifyUri(playerState.trackUri),
          updateOnCallback);
      nextSong();

      if(favourited) {
        Log.d("KiaraPlayerEvent", "Track actually favourited");
        application.learningApi().trackFavourited(
            application.userId(),
            playlistId,
            stripSpotifyUri(playerState.trackUri),
            updateOnCallback);
      }

    // TRACK_END: the song was skipped (nextSong() was called)
    } else if (eventType == EventType.TRACK_END) {
      double proportion = (double)lastSkip / (double)duration;
      if(proportion < 0.98) {
        Log.d("KiaraPlayerEvent", "Track ended due to skip " + playerState.trackUri + " " + proportion);
        application.learningApi().trackSkipped(
            application.userId(),
            playlistId,
            stripSpotifyUri(playerState.trackUri),
            lastSkip,
            emptyCallback);

        if(favourited) {
          Log.d("KiaraPlayerEvent", "Track actually favourited");
          application.learningApi().trackFavourited(
              application.userId(),
              playlistId,
              stripSpotifyUri(playerState.trackUri),
              updateOnCallback);
         }
      }

    } else if (eventType == EventType.LOST_PERMISSION) {
        Log.d("KiaraPlayerEvent", "Lost permission, pausing.");

    } else if (eventType == EventType.SKIP_NEXT) {
      Log.d("KiaraPlayerEvent", "Skipping to next song. " + lastSkip);
      application.learningApi().trackSkipped(
          application.userId(),
          playlistId,
          stripSpotifyUri(playerState.trackUri),
          lastSkip,
          updateOnCallback);
    }

    application.getBus().post(new PlaybackEvent(eventType, playerState));
  }

  @Override
  public void onPlaybackError(ErrorType errorType, String s) {
    Log.e(log, s);
  }

  @Override
  public void onPlayerState(PlayerState playerState) {
    duration = playerState.durationInMs;
    position = playerState.positionInMs;
    application.getBus().post(playerState);
  }

  private void initialiseHandlers() {
    mRunnable = new Runnable() {
      @Override
      public void run() {
        if(player != null && playing) {
          player.getPlayerState(MusicService.this);
        }
        mHandler.postDelayed(this, 500);
      }
    };
  }


  // Music Playback Control Methods

  public void playSong(Song song) {
    Log.d(log, "Playing song " + song.getSongName());

    // update state regarding the playback
    playing = true;
    favourited = false;
    currentSong = song;

    // play song on the spotify player
    player.play("spotify:track:" + song.getSpotifyId());


    // signal playback session is in progress, runnable, acquire wake locks etc.
    sharedPreferences().edit().putBoolean(Constants.IN_SESSION, true).commit();
    mHandler.post(mRunnable);
    acquireLocks();

    startForeground(Constants.MUSIC_NOTIF_ID, buildNotif(playing, favourited).build());
    // update the notification album image
    albumNotif();
  }

  public void playSongWeak(Song song) {

    // Either not playing a song, or we want to play a different song
    if(currentSong == null || !currentSong.getSpotifyId().equals(song.getSpotifyId())) {
      playSong(song);
    } else {
      if(isPlaying()) {
        // pass
      } else {
        resumeSong();
      }
    }
    acquireLocks();
  }

  public void resumeSong() {
    Log.d(log, "Resuming song.");
    if(currentSong != null) {
      Log.d(log, "Current song is not null. " + currentSong.getSongName());
      player.resume();
      playing = true;
      mHandler.post(mRunnable);
      acquireLocks();
    }
  }

  public void pause() {
    Log.d(log, "Pausing song.");
    player.pause();
    player.getPlayerState(this);
    playing = false;
    mHandler.removeCallbacks(mRunnable);
    releaseLocks();
  }

  public void nextSong() {
    if(playQueue.size() > 0) {
      lastSkip = position;
      Log.d(log, "playing the nextSong " + playQueue.getFirst().getSongName());
      playSong(playQueue.getFirst());
      playQueue.removeFirst();
    } else {
      playing = false;
      pause();
    }
    favourited = false;
  }

  public void previousSong() {
    player.skipToPrevious();
    favourited = false;
  }

  public void queueSong(Song song) {
    playQueue.addLast(song);
    application.learningApi().trackQueued(
        application.userId(),
        playlistId,
        currentSong.getSpotifyId(),
        song.getSpotifyId(),
        updateOnCallback);
  }

  public RepeatState repeat() {
    if(repeating == RepeatState.FALSE) {
      repeating = RepeatState.ONE;
      player.setRepeat(true);
    } else if(repeating == RepeatState.ONE) {
      repeating  = RepeatState.TRUE;
      player.setRepeat(true);
    } else if(repeating == RepeatState.TRUE) {
      repeating = RepeatState.FALSE;
      player.setRepeat(false);
    }
    return repeating;
  }

  @Subscribe
  public void onQueueRequest(QueueSongRequest request) {
    queueSong(request.getSong());
  }

  /**
   * toggle the playback status for the player.
   *
   * @return  true if the player is currently playing a song.
   */
  public boolean pauseplay() {
    if(playing) {
      // Pause the player
      pause();
    } else {
      // Start playing
      resumeSong();
    }
    updateNotif(playing, favourited);
    return playing;
  }

  /**
   * toggle whether the current song is favourited or not.
   */
  public boolean toggleFav() {
    favourited = !favourited;
    Log.d(log, currentSong.getSongName() + " favourited ? " + favourited);
    updateNotif(playing, favourited);
    application.getBus().post(new Favourite(favourited));
    return favourited;
  }

  @Subscribe
  public void onProgressChanged(SeekbarProgressChanged event) {
    if(player != null && event.fromUser) {
      int position = (int)(duration * ((float)event.progress/255.0));
      Log.d(log, "seeking to " + (duration*(event.progress/255)) + " " + position);
      player.seekToPosition(position);
    }
  }

  private void acquireLocks() {
    wakeLock.acquire();
    wifiLock.acquire();
  }

  private void releaseLocks() {
    wakeLock.release();
    wifiLock.release();
  }

  public void albumNotif() {
    Target target = new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        currentSongAlbumCover = bitmap;
        updateNotif(currentSongAlbumCover);
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {
      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {
      }
    };
    Picasso.with(this).load(currentSong.getImageURL()).into(target);
  }

  // TODO(yojan) tweak : change drawables, text etc.
  private NotificationCompat.Builder buildNotif(boolean playing, boolean favourited) {
    String text = currentSong.getArtistName() + " - " + currentSong.getSongName();

    Intent i = new Intent(this, PlayerActivity.class);
    i.putExtra(Constants.ARG_SONG, new SongParcelable(currentSong));
    PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent stopIntent = new Intent(this, MusicService.class);
    stopIntent.setAction(Constants.ACTION_STOP_SERVICE);
    PendingIntent stopService = PendingIntent.getService(this, 0, stopIntent, 0);

    Intent playpauseIntent = new Intent(this, MusicService.class);
    playpauseIntent.setAction(Constants.ACTION_PLAY_PAUSE);
    PendingIntent playpauseService = PendingIntent.getService(this, 0, playpauseIntent, 0);

    Intent favIntent = new Intent(this, MusicService.class);
    favIntent.setAction(Constants.ACTION_FAVOURITE);
    PendingIntent favService = PendingIntent.getService(this, 0, favIntent, 0);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
        .setContentTitle(currentSong.getSongName())
        .setContentIntent(pi)
        .setSmallIcon(R.drawable.ic_new_releases_white_24dp)
        .setContentText(currentSong.getArtistName() + " - " + currentSong.getAlbumName())
        .addAction(R.drawable.ic_close_white_24dp, "Stop", stopService);

    notificationBuilder.addAction(
        favourited ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp,
        "Favourite", favService
    );

    notificationBuilder.addAction(
        playing ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
        playing ? "Pause" : "Play",
        playpauseService
    );

    if(currentSongAlbumCover != null) {
      notificationBuilder.setLargeIcon(currentSongAlbumCover);
    }

    return notificationBuilder;
  }

  private void updateNotif(boolean playing, boolean favourited) {
    mNotificationManager.notify(Constants.MUSIC_NOTIF_ID, buildNotif(playing, favourited).build());
  }

  private void updateNotif(Bitmap largeIcon) {
    Log.d(log, "updating notification with a large icon");
    mNotificationManager.notify(Constants.MUSIC_NOTIF_ID, buildNotif(playing, favourited).setLargeIcon(largeIcon).build());
  }

  @Override
  public void onAudioFocusChange(int focusChange) {
    if(focusChange == AudioManager.AUDIOFOCUS_GAIN) {
      // resume playback
      Log.i(log, "Audio Focus Changed: Gain");
      player.resume();
    } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS) {
      // Lost focus for an unbounded amount of time: stop playback and release media player
      Log.i(log, "Audio Focus Changed: Loss");
      player.pause();
    } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
      // Lost focus for a short time, but we have to stop
      // playback. We don't release the media player because playback
      // is likely to resume
      Log.i(log, "Audio Focus Changed: Transient");
      player.pause();
    } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
      // Lost focus for a short time, but it's ok to keep playing
      // at an attenuated level
      // TODO(yojan): attenuate once spotify implements volume control.
      Log.i(log, "Audio Focus Changed: Loss Transient Can Duck");
    }
  }

  public Song getCurrentSong() {
    if(currentSong != null)
      Log.d(log, "Current song is " + currentSong.getSongName());
    else
      Log.d(log, "Current song is null");
    return currentSong;
  }

  public boolean isPlaying() {
    return playing;
  }

  private static String stripSpotifyUri(String spotifyUri) {
    return spotifyUri.substring("spotify:track:".length());
  }

  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class MusicBinder extends Binder {
    public MusicService getService() {
      return MusicService.this;
    }
  }

  retrofit.Callback<Song> updateOnCallback =  new retrofit.Callback<Song>() {
    @Override
    public void success(Song s, Response response) {
      playQueue.clear();
      playQueue.add(s);
      application.getBus().post(s);
      Log.d(log, playQueue.size() + " pqueue size");
    }

    @Override
    public void failure(RetrofitError error) {
      Log.e(log, error.getMessage());
    }
  };

  retrofit.Callback<Song> emptyCallback = new Callback<Song>() {
    @Override
    public void success(Song song, Response response) {
//      Log.d(log, song.getSongName());
    }

    @Override
    public void failure(RetrofitError error) {

    }
  };
}
