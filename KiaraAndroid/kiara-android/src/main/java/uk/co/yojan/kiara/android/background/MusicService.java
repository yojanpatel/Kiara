package uk.co.yojan.kiara.android.background;

import android.app.Notification;
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
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.playback.PlayerStateCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.events.SeekbarProgressChanged;
import uk.co.yojan.kiara.client.data.Song;

/**
 * Background, long-running service that handles the Spotify streaming and
 * callbacks to the UI threads.
 */
public class MusicService extends Service
    implements PlayerNotificationCallback, PlayerStateCallback, AudioManager.OnAudioFocusChangeListener {

  private static final String log = MusicService.class.getName();

  private KiaraApplication application;

  private Player player;
  private boolean playing;
  private boolean favourited;

  private Song currentSong;
  private int duration;
  private Bitmap currentSongAlbumCover;
  private Song predictedNextSong;

  // Locks that have to be acquired from Android if streaming is taking place.
  private PowerManager.WakeLock wakeLock;
  private WifiManager.WifiLock wifiLock;

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

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(log, "onStartCommand");

    if(intent.getBooleanExtra(Constants.PLAY_ACTION, false)) {
      String spotifyUri = intent.getStringExtra(Constants.SONG_URI);
      if(spotifyUri != null) {
//        playSong(spotifyUri);
      }
    } else if(intent.getAction() != null) {
      if (intent.getAction().equals(Constants.ACTION_STOP_SERVICE)) {
        Log.d(log, "Stopping service from intent.");
        stopSelf();
      } else if (intent.getAction().equals(Constants.ACTION_PLAY_PAUSE)) {
        Log.d(log, "play/pause from intent.");
        pauseplay();
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
    Log.d(log, "initialising player");
    String accessToken = sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
    if(accessToken != null) {
      Spotify spotify = new Spotify(accessToken);
      player = spotify.getPlayer(this, "Kiara", this,
          new Player.InitializationObserver() {

            @Override
            public void onInitialized() {
              Log.d(log, "onInitialized");
              player.addPlayerNotificationCallback(MusicService.this);
            }

            @Override
            public void onError(Throwable throwable) {
              Log.e(log, throwable.getMessage());
            }
          });
    }
  }

  @Override
  public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
    Log.d(log, "Playback event received " + eventType);
    switch(eventType) {
      default:
        break;
    }

    application.getBus().post(eventType);
  }

  @Override
  public void onPlayerState(PlayerState playerState) {
    Log.d(log, "playerState received. " + playerState.durationInMs);
    duration = playerState.durationInMs;
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


  public void playSong(String spotifyUri) {
    Log.d(log, "Playing Song " + spotifyUri);
    playing = true;
    player.play("spotify:track:"+spotifyUri);
    mHandler.post(mRunnable);
    acquireLocks();
  }

  public void playSong(Song song) {
    Log.d(log, "Playing song " + song.getSongName());
    playing = true;
    sharedPreferences().edit().putBoolean(Constants.IN_SESSION, true).commit();
    currentSong = song;
    player.play("spotify:track:" + song.getSpotifyId());

    mHandler.post(mRunnable);
    acquireLocks();
  }

  public void playSongWeak(Song song) {
    playing = true;
    if(currentSong == null || !currentSong.getSpotifyId().equals(song.getSpotifyId())) {
      currentSong = song;
      player.play("spotify:track:" + song.getSpotifyId());
    } else {
      if(isPlaying()) {
        // pass
      } else {
        player.resume();
      }
    }

    sharedPreferences().edit().putBoolean(Constants.IN_SESSION, true).commit();
    playing = true;
    mHandler.post(mRunnable);
    acquireLocks();
  }

  /* TODO(yojan): event transfer between app and Kiara. */

  public void nextSong() {
    player.skipToNext();
    favourited = false;
    /* post event to kiara saying song was skipped, with duration etc. */
  }

  public void previousSong() {
    player.skipToPrevious();
    favourited = false;
    /* post event to kiara saying song was replayed, with duration etc. */
  }

  public void queueSong(String spotifyUri) {
    player.queue(spotifyUri);
    /* post event to kiara saying song was queued. */
  }

  /**
   * toggle the playback status for the player.
   *
   * @return  true if the player is currently playing a song.
   */
  public boolean pauseplay() {
    if(playing) {
      // Pause the player
      player.pause();
      player.getPlayerState(this);
      playing = !playing;
      releaseLocks();
    } else {
      // Start playing
      player.resume();
      playing = !playing;
      acquireLocks();
    }
    return playing;
  }

  /**
   * toggle whether the current song is favourited or not.
   *
   */
  public boolean toggleFav() {
    favourited = !favourited;
    Log.d(log, currentSong.getSongName() + " favourited ? " + favourited);
    return favourited;
  }

  @Subscribe
  public void onProgressChanged(SeekbarProgressChanged event) {
    if(player != null && event.fromUser) {
      Log.d(log, "progress changed: " + duration + " " + event.progress);
      int position = (int)(duration * ((float)event.progress/255.0));
      Log.d(log, "seeking to " + (duration*(event.progress/255)) + " " + position);
      player.seekToPosition(position);
    }
  }

  private void acquireLocks() {
    Log.i(log, "Acquiring Wake and Wifi Locks.");
    wakeLock.acquire();
    wifiLock.acquire();
  }

  private void releaseLocks() {
    Log.i(log, "Releasing Wake and Wifi Locks.");
    wakeLock.release();
    wifiLock.release();
  }

  public void startForeground() {
    Target target = new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        currentSongAlbumCover = bitmap;
        startForeground(37, buildNotif());
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
  private Notification buildNotif() {
    String text = currentSong.getArtistName() + " - " + currentSong.getSongName();

//    PendingIntent pi = PendingIntent.getActivity(this, 0,
//        new Intent(this, MainActivity.class),
//        PendingIntent.FLAG_UPDATE_CURRENT);

    Intent stopIntent = new Intent(this, MusicService.class);
    stopIntent.setAction(Constants.ACTION_STOP_SERVICE);
    PendingIntent stopService = PendingIntent.getService(this, 0, stopIntent, 0);

    Intent playpauseIntent = new Intent(this, MusicService.class);
    playpauseIntent.setAction(Constants.ACTION_PLAY_PAUSE);
    PendingIntent playpauseService = PendingIntent.getService(this, 0, playpauseIntent, 0);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    Notification notification = builder
        .setContentTitle(currentSong.getSongName())
        .setSmallIcon(R.drawable.ic_play_arrow_white_18dp)
        .setLargeIcon(currentSongAlbumCover)
        .setContentText(currentSong.getArtistName() + " - " + currentSong.getAlbumName())
        .addAction(R.drawable.ic_close_white_24dp, "Stop", stopService)
        .addAction(R.drawable.ic_shuffle_white_24dp, "Pause/Play", playpauseService)
        .build();
    return notification;
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

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class MusicBinder extends Binder {
    public MusicService getService() {
      return MusicService.this;
    }
  }
}
