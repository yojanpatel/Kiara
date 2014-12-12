package uk.co.yojan.kiara.android.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.playback.PlayerStateCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.android.background.MusicStateCallback;
import uk.co.yojan.kiara.android.events.SeekbarProgressChanged;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

/**
 * Fragment that handles the song streaming from Spotify.
 */
public class PlayerFragment extends KiaraFragment implements PlayerNotificationCallback, PlayerStateCallback, MusicStateCallback {

  private static final String log = PlayerFragment.class.getName();
  public static final String SONG_PARAM = "SONG";

  private KiaraActivity parent;
  private Context mContext;
  private static Picasso picasso;

  private boolean bound;

  @InjectView(R.id.album_image) ImageView albumArt;
  @InjectView(R.id.song_name) TextView songName;
  @InjectView(R.id.artist_name) TextView artistName;
  @InjectView(R.id.album_name) TextView albumName;
  @InjectView(R.id.seekBar) SeekBar seekBar;

  @InjectView(R.id.playpause) ImageButton playpause;
  @InjectView(R.id.favouritefab) FloatingActionButton favouriteFab;
  @InjectView(R.id.elapsed) TextView elapsed;

  private Song currentSong;

  MusicService musicService;

  Drawable pause;
  Drawable play;
  Drawable favOutline;
  Drawable favFilled;

  private int currentPosition;
  private int duration;

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
    }

    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_player, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    parent = (KiaraActivity) getActivity();
    Resources res = getResources();
    pause = res.getDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
    play = res.getDrawable(R.drawable.ic_play_circle_fill_white_48dp);
    favOutline = res.getDrawable(R.drawable.ic_favorite_outline_white_24dp);
    favFilled = res.getDrawable(R.drawable.ic_favorite_white_24dp);


    Intent startService = new Intent(mContext, MusicService.class);
    parent.startService(startService);
    Intent bind = new Intent(mContext, MusicService.class);
    parent.bindService(bind, mConnection, Context.BIND_AUTO_CREATE);

    initButtons();
    initialiseSeekBar();

    picasso.load(currentSong.getImageURL()).into(albumArt);
    songName.setText(currentSong.getSongName());
    artistName.setText(currentSong.getArtistName());
    albumName.setText(currentSong.getAlbumName());

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
    Spotify.destroyPlayer(this);
    if (bound) {
      parent.unbindService(mConnection);
      bound = false;
    }
  }

  private void initialiseSeekBar() {
    seekBar.setMax(255);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
       getBus().post(new SeekbarProgressChanged(seekBar, progress, fromUser));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });
  }

  private void initButtons() {
    playpause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(musicService.pauseplay()) {
          // Playing
          playpause.setImageDrawable(pause);
        } else {
          // Paused
          playpause.setImageDrawable(play);
        }
      }
    });


    favouriteFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // favourited
        if(musicService.toggleFav()) {
          favouriteFab.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
          favouriteFab.setIcon(R.drawable.ic_favorite_outline_white_24dp);
        }
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
  public void onPlayingStateChanged(boolean nowPlaying) {
    if(nowPlaying) {
      // player is now playing.
      playpause.setImageDrawable(pause);
    } else {
      // player is now paused.
      playpause.setImageDrawable(play);
    }
  }

  @Subscribe
  @Override
  public void onPlayerState(PlayerState playerState) {
    currentPosition = playerState.positionInMs;
    duration = playerState.durationInMs;
    Log.d(log, "playerState received. " + playerState.durationInMs);
    if(duration > 0) {
      seekBar.setProgress((currentPosition * 255) / duration);

      currentPosition /= 1000; // convert to seconds
      int hours = currentPosition / 3600;
      currentPosition  = currentPosition % 3600;
      int mins = currentPosition / 60;
      int seconds = currentPosition % 60;
      String elapsedText  = "";
      if(hours > 0) elapsedText = hours + ".";
      elapsedText += mins + "." + seconds;
      elapsed.setText(elapsedText);
    }
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      Log.d("KiaraActivity", "serviceConnected");
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
      musicService = binder.getService();
      bound = true;
      musicService.setCurrentSong(currentSong);
      musicService.startForeground();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      bound = false;
    }
  };
}
