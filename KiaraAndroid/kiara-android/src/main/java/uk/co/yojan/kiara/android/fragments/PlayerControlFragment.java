package uk.co.yojan.kiara.android.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.client.data.Song;


public class PlayerControlFragment extends KiaraFragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  private Context mContext;

  private boolean bound;
  private MusicService musicService;
  private Song currentSong;


  @InjectView(R.id.album_image) ImageView albumImage;
  @InjectView(R.id.song_name) TextView songName;
  @InjectView(R.id.detail) TextView detail;
  @InjectView(R.id.playpause)ImageButton playpause;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment PlayerControlFragment.
   */
  public static PlayerControlFragment newInstance(/*String param1, String param2*/) {
    PlayerControlFragment fragment = new PlayerControlFragment();
    Bundle args = new Bundle();
//    args.putString(ARG_PARAM1, param1);
//    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  public PlayerControlFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    if (getArguments() != null) {
//      mParam1 = getArguments().getString(ARG_PARAM1);
//      mParam2 = getArguments().getString(ARG_PARAM2);
//    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_player_control, container, false);
    mContext = rootView.getContext();

    Intent bind = new Intent(mContext, MusicService.class);
    getKiaraActivity().bindService(bind, mConnection, Context.BIND_AUTO_CREATE);
    ButterKnife.inject(this, rootView);

    playpause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean playing = musicService.pauseplay();
        setPlayPauseImageResource(playing);
      }
    });

    // Inflate the layout for this fragment
    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
    if (bound) {
      getKiaraActivity().unbindService(mConnection);
      bound = false;
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  private void updateCurrentSong(Song currentSong) {
    this.currentSong = currentSong;
    if(currentSong != null) {
      Picasso.with(mContext).load(currentSong.getImageURL()).resize(150, 150).into(albumImage);
      songName.setText(currentSong.getSongName());
      detail.setText(currentSong.getArtistName() + " - " + currentSong.getAlbumName());
    } else {
      Log.d("PlayerControlFragment", "updateCurrentSong with null, leaving it.");
      getView().setVisibility(View.GONE);
    }
  }

  private void setPlayPauseImageResource(boolean playing) {
    if(playing) {
      playpause.setImageResource(R.drawable.ic_pause_grey300_48dp);
    } else {
      playpause.setImageResource(R.drawable.ic_play_arrow_grey300_48dp);
    }
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      Log.d("PlayerControlFragment", "serviceConnected");
      bound = true;
      MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
      musicService = binder.getService();

      updateCurrentSong(musicService.getCurrentSong());
      setPlayPauseImageResource(musicService.isPlaying());
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      bound = false;
    }
  };
}
