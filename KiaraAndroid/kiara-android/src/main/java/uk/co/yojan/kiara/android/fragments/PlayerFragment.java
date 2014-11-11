package uk.co.yojan.kiara.android.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.playback.PlayerStateCallback;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.android.views.FloatingActionButton;
import uk.co.yojan.kiara.client.data.Song;

/**
 * Fragment that handles the song streaming from Spotify.
 */
public class PlayerFragment extends KiaraFragment implements PlayerNotificationCallback, PlayerStateCallback {

  private static final String log = PlayerFragment.class.getName();
  public static final String SONG_PARAM = "SONG";

  private KiaraActivity parent;
  private Context mContext;
  private static Picasso picasso;

  @InjectView(R.id.album_image) ImageView albumArt;
  @InjectView(R.id.song_name) TextView songName;
  @InjectView(R.id.artist_name) TextView artistName;
  @InjectView(R.id.album_name) TextView albumName;
  @InjectView(R.id.seekBar) SeekBar seekBar;

  private FloatingActionButton fab;
  private Song currentSong;
  private String songSpotifyId;
  private Player player;
  private boolean playing = true;
  private int currentPosition = 0;
  private int duration;

  private Handler mHandler = new Handler();
  private Runnable mRunnable = new Runnable() {
    @Override
    public void run() {
      if(player != null && playing) {
        player.getPlayerState(PlayerFragment.this);
      }
      mHandler.postDelayed(this, 1000);
    }
  };

  public static PlayerFragment newInstance(Song song) {
    PlayerFragment fragment =  new PlayerFragment();
    Bundle args = new Bundle();
    args.putParcelable(SONG_PARAM, new SongParcelable(song));
    fragment.setArguments(args);
    return fragment;
  }


  public PlayerFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      currentSong = (Song)getArguments().getParcelable(SONG_PARAM);
      songSpotifyId = "spotify:track:"+currentSong.getSpotifyId();
      Log.d(log, currentSong.getArtistName());
    }

    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_player, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    parent = (KiaraActivity) getActivity();

    setUpFab();
    initialisePlayer();

    picasso.load(currentSong.getImageURL()).into(albumArt);
    songName.setText(currentSong.getSongName());
    artistName.setText(currentSong.getArtistName());
    albumName.setText(currentSong.getAlbumName());

    seekBar.setMax(255);
    seekBar.getThumb().setAlpha(0);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(player != null && fromUser) {
          Log.d(log, "progress changed: " + duration + " " + progress);
          int position = (int)(duration * ((float)progress/255.0));
          Log.d(log, "seeking to " + (duration*(progress/255)) + " " + position);
          player.seekToPosition(position);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    return rootView;
  }


  private void initialisePlayer() {
    String accessToken = parent.sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
    if(accessToken != null) {
      Spotify spotify = new Spotify(accessToken);
      player = spotify.getPlayer(mContext, "Kiara", this,
          new Player.InitializationObserver() {

            @Override
            public void onInitialized() {
              Log.d(log, "onInitialized");
              player.addPlayerNotificationCallback(PlayerFragment.this);
              player.play(songSpotifyId);
            }

            @Override
            public void onError(Throwable throwable) {
              Log.e(log, throwable.getMessage());
              Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
          });
      mHandler.post(mRunnable);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
    Spotify.destroyPlayer(this);
  }

  private void pauseplay() {

    if(playing) {
      // Pause the player
      player.pause();
      player.getPlayerState(this);
      Drawable play = getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp);
      fab.setFloatingActionButtonDrawable(play);
      playing = !playing;
    } else {
      // Start playing
      Drawable pause = getResources().getDrawable(R.drawable.ic_pause_white_24dp);
      fab.setFloatingActionButtonDrawable(pause);
      player.resume();
      playing = !playing;
    }
  }

  private void setUpFab() {
    Drawable pause = getResources().getDrawable(R.drawable.ic_pause_white_24dp);
    fab = new FloatingActionButton.Builder(getActivity())
        .withButtonColor(getResources().getColor(R.color.pinkA200))
        .withDrawable(pause)
        .withGravity(Gravity.RIGHT | Gravity.BOTTOM)
        .withMargins(0, 0, 0, 24)
        .create();

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pauseplay();
      }
    });
  }

  @Override
  public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
    Log.d(log, "Playback event received " + eventType.name());
    switch(eventType) {
      default:
        break;
    }
  }

  @Override
  public void onPlayerState(PlayerState playerState) {
    currentPosition = playerState.positionInMs;
    duration = playerState.durationInMs;
    Log.d(log, "playerState received. " + playerState.durationInMs);
    if(duration > 0) {
      seekBar.setProgress((currentPosition * 255) / duration);
    }
  }
}
