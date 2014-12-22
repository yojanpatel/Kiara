package uk.co.yojan.kiara.android.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.android.events.PlaybackEvent;
import uk.co.yojan.kiara.android.events.SeekbarProgressChanged;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

/**
 * Fragment that handles the song streaming from Spotify.
 */
public class PlayerFragment extends KiaraFragment {

  private static final String log = PlayerFragment.class.getName();

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
  @InjectView(R.id.prev_track) ImageButton previousTrackButton;
  @InjectView(R.id.next_track) ImageButton nextTrackButton;
  @InjectView(R.id.replay_track) ImageButton repeatButton;

  private long playlistId;
  private Song currentSong;

  MusicService musicService;

  Drawable pause;
  Drawable play;
  Drawable favOutline;
  Drawable favFilled;
  Drawable repeatOne;
  Drawable repeat;

  private int currentPosition;
  private int duration;

  public static PlayerFragment newInstance(long playlistId, Song song) {
    PlayerFragment fragment =  new PlayerFragment();
    Bundle args = new Bundle();
    args.putParcelable(Constants.ARG_SONG, new SongParcelable(song));
    args.putLong(Constants.ARG_PLAYLIST_ID, playlistId);
    fragment.setArguments(args);
    return fragment;
  }


  public PlayerFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      currentSong = getArguments().getParcelable(Constants.ARG_SONG);
      playlistId = getArguments().getLong(Constants.ARG_PLAYLIST_ID);
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

    Resources res = getResources();
    pause = res.getDrawable(R.drawable.ic_pause_circle_fill_white_48dp);
    play = res.getDrawable(R.drawable.ic_play_circle_fill_white_48dp);
    favOutline = res.getDrawable(R.drawable.ic_favorite_outline_white_24dp);
    favFilled = res.getDrawable(R.drawable.ic_favorite_white_24dp);

    repeatOne = res.getDrawable(R.drawable.ic_repeat_one_white_24dp);
    repeat = res.getDrawable(R.drawable.ic_repeat_white_24dp);
    if(Build.VERSION.SDK_INT >= 16) {
      repeatOne.setColorFilter(
          getResources().getColor(R.color.pinkA200),
          PorterDuff.Mode.SRC_IN);

      repeat.setColorFilter(
          getResources().getColor(R.color.pinkA400),
          PorterDuff.Mode.SRC_IN);
    }

    if(Build.VERSION.SDK_INT >= 21) {
      albumArt.setBackground(mContext.getDrawable(R.drawable.ripple));
    }


    Intent startService = new Intent(mContext, MusicService.class);
    getKiaraActivity().startService(startService);
    Intent bind = new Intent(mContext, MusicService.class);
    getKiaraActivity().bindService(bind, mConnection, Context.BIND_AUTO_CREATE);

    initButtons();
    initialiseSeekBar();
    updateUi(currentSong);

    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
    Spotify.destroyPlayer(this);
    if (bound) {
      getKiaraActivity().unbindService(mConnection);
      bound = false;
    }
  }

  private void initialiseSeekBar() {
    seekBar.setMax(255);

    if(Build.VERSION.SDK_INT >= 16) {
      seekBar.getThumb().setColorFilter(
          getResources().getColor(R.color.pinkA200),
          PorterDuff.Mode.SRC_IN);

      seekBar.getProgressDrawable().setColorFilter(
          getResources().getColor(R.color.pinkA400),
          PorterDuff.Mode.SRC_IN);
    }

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        getBus().post(new SeekbarProgressChanged(seekBar, progress, fromUser));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private void initButtons() {
    playpause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        musicService.pauseplay();
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

    nextTrackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        musicService.nextSong();
      }
    });

    previousTrackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        musicService.previousSong();
      }
    });

    repeatButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MusicService.RepeatState repeatState = musicService.repeat();
        if (repeatState == MusicService.RepeatState.FALSE) {
          repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
        }
        if (repeatState == MusicService.RepeatState.ONE) {
          repeatButton.setImageDrawable(repeatOne);
        }
        if (repeatState == MusicService.RepeatState.TRUE) {
          repeatButton.setImageDrawable(repeat);
        }
      }
    });
  }

  private void updateUi(Song currentSong) {
    this.currentSong = currentSong;
    picasso.load(currentSong.getImageURL()).into(albumArt);
    songName.setText(currentSong.getSongName());
    artistName.setText(currentSong.getArtistName());
    albumName.setText(currentSong.getAlbumName());
  }

  @Subscribe
  public void onPlaybackEvent(PlaybackEvent event) {
    Log.d(log, "PlaybackEvent " + event.getEvent().toString());
    PlayerNotificationCallback.EventType eventType = event.getEvent();
    PlayerState state = event.getState();

    if(eventType == PlayerNotificationCallback.EventType.PLAY ||
       eventType == PlayerNotificationCallback.EventType.TRACK_START) {
      playpause.setImageDrawable(pause);

      updateUi(musicService.getCurrentSong());

    } else if(eventType == PlayerNotificationCallback.EventType.PAUSE ||
              eventType == PlayerNotificationCallback.EventType.LOST_PERMISSION) {
      playpause.setImageDrawable(play);
    }
  }

  @Subscribe
  public void onPlayerState(PlayerState playerState) {
    currentPosition = playerState.positionInMs;
    duration = playerState.durationInMs;
    if(duration > 0) {
      seekBar.setProgress((currentPosition * 255) / duration);

      currentPosition /= 1000; // convert to seconds
      int hours = currentPosition / 3600;
      currentPosition  = currentPosition % 3600;
      int mins = currentPosition / 60;
      int seconds = currentPosition % 60;
      String elapsedText  = "";
      if(hours > 0) elapsedText = hours + ".";
      elapsedText += mins + "." + (seconds < 10 ? "0" : "") + seconds;
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

      musicService.setPlaylistId(playlistId);
      Log.d("PLAYLISTID", playlistId + " ");
      musicService.playSongWeak(currentSong);
      playpause.setImageDrawable(
          musicService.isPlaying() ? pause : play
      );
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      bound = false;
    }
  };
}
