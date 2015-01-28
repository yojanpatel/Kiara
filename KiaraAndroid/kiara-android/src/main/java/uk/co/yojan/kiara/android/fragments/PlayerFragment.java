package uk.co.yojan.kiara.android.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.android.events.Favourite;
import uk.co.yojan.kiara.android.events.PlaybackEvent;
import uk.co.yojan.kiara.android.events.SeekbarProgressChanged;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.android.utils.PaletteTransformation;
import uk.co.yojan.kiara.android.views.CircleButton;
import uk.co.yojan.kiara.android.views.IconButton;
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

  @InjectView(R.id.playpause) CircleButton playpause;
  @InjectView(R.id.favouritefab) FloatingActionButton favouriteFab;
  @InjectView(R.id.elapsed) TextView elapsed;
  @InjectView(R.id.prev_track) IconButton previousTrackButton;
  @InjectView(R.id.next_track) IconButton nextTrackButton;
  @InjectView(R.id.replay_track) IconButton repeatButton;

  private long playlistId;
  private Song currentSong;

  MusicService musicService;

  Drawable pause;
  Drawable play;
  Drawable favOutline;
  Drawable favFilled;
  Drawable repeatOne;
  Drawable repeat;

  private int accentpink;
  private int darkgrey;

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


  public PlayerFragment() {}

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
    pause = res.getDrawable(R.drawable.ic_pause_white_36dp);
    play = res.getDrawable(R.drawable.ic_play_arrow_white_36dp);
    favOutline = res.getDrawable(R.drawable.ic_favorite_outline_white_24dp);
    favFilled = res.getDrawable(R.drawable.ic_favorite_white_24dp);

    accentpink = getResources().getColor(R.color.pinkA200);
    darkgrey = getResources().getColor(R.color.grey900);

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

    picasso.load(currentSong.getImageURL()).transform(PaletteTransformation.instance())
        .into(albumArt, new Callback.EmptyCallback() {
          @Override
          public void onSuccess() {
            Bitmap bitmap = ((BitmapDrawable) albumArt.getDrawable()).getBitmap(); // Ew!
            Palette palette = PaletteTransformation.getPalette(bitmap);
            updateColours(palette);
          }
        });
    songName.setText(currentSong.getSongName());
    artistName.setText(currentSong.getArtistName());
    albumName.setText(currentSong.getAlbumName());

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    updateUi(currentSong);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
    Spotify.destroyPlayer(this);
    if (bound) {
      musicService.unregisterPlayerFragment();
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

  private void updateUi(Song newSong) {
    if(this.currentSong == null || !currentSong.getSpotifyId().equals(newSong.getSpotifyId())) {
      currentSong = newSong;
      picasso.load(newSong.getImageURL()).transform(PaletteTransformation.instance())
          .into(albumArt, new Callback.EmptyCallback() {
        @Override
        public void onSuccess() {
          Bitmap bitmap = ((BitmapDrawable) albumArt.getDrawable()).getBitmap(); // Ew!
          Palette palette = PaletteTransformation.getPalette(bitmap);
          updateColours(palette);
        }
      });
      songName.setText(newSong.getSongName());
      artistName.setText(newSong.getArtistName());
      albumName.setText(newSong.getAlbumName());
    }
  }

  private void updateColours(Palette palette) {

    int accentColour =
        palette.getVibrantColor(
          palette.getDarkVibrantColor(
            palette.getMutedColor(
                palette.getDarkMutedColor(accentpink))));
    Log.d("PALETTE", palette.getSwatches().toString());
    Log.d("PALETTE", accentColour + " Accent colour");

    int darkColour = palette.getDarkMutedColor(palette.getDarkVibrantColor(darkgrey));

    int textColour = palette.getLightMutedColor(palette.getLightVibrantColor(-1));

    // ensure the colours extracted are bright for the buttons.
    float[] hsv = new float[3];
    Color.colorToHSV(accentColour, hsv);
    if(hsv[1] < 0.1) accentColour = accentpink;

    Color.colorToHSV(darkColour, hsv);
    if(hsv[2] > 0.32) darkColour = darkgrey;

    seekBar.getProgressDrawable().setColorFilter(accentColour, PorterDuff.Mode.SRC_IN);
    seekBar.getThumb().setColorFilter(accentColour, PorterDuff.Mode.SRC_IN);
    favouriteFab.setColorNormal(accentColour);
    playpause.setColor(accentColour);

    if(textColour != -1) {
      float[] hsv2 = new float[3];
      Color.colorToHSV(textColour, hsv2);

      hsv2[2] += (1.0f - hsv2[2]) / 1.6f;
      int brighterText = Color.HSVToColor(hsv2);

      // brighten for title
      songName.setTextColor(brighterText);

      //default for artist/album text
      albumName.setTextColor(textColour);
      artistName.setTextColor(textColour);
    }

    if(getView() != null) {
      getView().setBackgroundColor(darkColour);
    }
  }

  public void onFavourite(Favourite favourite) {
    if(favourite.isFavourited()) {
      favouriteFab.setIcon(R.drawable.ic_favorite_white_24dp);
    } else {
      favouriteFab.setIcon(R.drawable.ic_favorite_outline_white_24dp);
    }
  }

  public void onPlaybackEvent(PlaybackEvent event) {
    Log.d(log, "PlaybackEvent " + event.getEvent().toString());
    PlayerNotificationCallback.EventType eventType = event.getEvent();
    PlayerState state = event.getState();

    if(eventType == PlayerNotificationCallback.EventType.PLAY) {
      playpause.setImageDrawable(pause);

    } else if(eventType == PlayerNotificationCallback.EventType.TRACK_START) {
      playpause.setImageDrawable(pause);
      favouriteFab.setIcon(R.drawable.ic_favorite_outline_white_24dp);
      seekBar.setProgress(0);
      updateUi(musicService.getCurrentSong());

    } else if(eventType == PlayerNotificationCallback.EventType.PAUSE) {
      playpause.setImageDrawable(play);
    } else if(eventType == PlayerNotificationCallback.EventType.LOST_PERMISSION) {
      playpause.setImageDrawable(play);
      Toast.makeText(mContext,
          "Kiara has been paused because your Spotify account is being used somewhere else.",
          Toast.LENGTH_LONG).show();
    }
  }

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

      // if session already in progress and arriving from control fragment
      if(playlistId == -1) {
        playlistId = musicService.getPlaylistId();
      } else {
        musicService.setPlaylistId(playlistId);
      }
      Log.d("PLAYLISTID", playlistId + " ");
      musicService.playSongWeak(currentSong);
      updateUi(musicService.getCurrentSong());
      playpause.setImageDrawable(
          musicService.isPlaying() ? pause : play
      );
      musicService.registerPlayerFragment(PlayerFragment.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      bound = false;
    }
  };
}
