package uk.co.yojan.kiara.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.activities.PlayerActivity;
import uk.co.yojan.kiara.android.adapters.SongListViewAdapter;
import uk.co.yojan.kiara.android.comparators.SongComparatorByArtist;
import uk.co.yojan.kiara.android.dialogs.AddSongDialog;
import uk.co.yojan.kiara.android.events.SongAdded;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.android.views.FloatingActionButton;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SongListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SongListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SongListFragment extends KiaraFragment {
  private static final String log = SongListFragment.class.getName();
  private static final String ID_PARAM = "id_param";
  private static final String SONG_PARAM = "song_param";

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private SongListViewAdapter mAdapter;

  @InjectView(R.id.progressBar) ProgressBar progressBar;
  @InjectView(R.id.song_recycler_view) RecyclerView mRecyclerView;

  private RecyclerView.LayoutManager mLayoutManager;
  private KiaraActivity parent;

  private ArrayList<SongParcelable> songs;
  private long id;

  /*
   * Factory method to create the fragment from the activity.
   * It allows easier transfer of data.
   */
  public static SongListFragment newInstance(long id, ArrayList<SongParcelable> songs) {
    SongListFragment fragment =  new SongListFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(SONG_PARAM, songs);
    args.putLong(ID_PARAM, id);
    fragment.setArguments(args);
    return fragment;
  }

  public SongListFragment() {
      // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      // The values passed from the playlist with songs.
      songs = getArguments().getParcelableArrayList(SONG_PARAM);
      Collections.sort(songs, new SongComparatorByArtist());
      id = getArguments().getLong(ID_PARAM);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_playlist_song_list, container, false);
    ButterKnife.inject(this, rootView);

    this.mContext = rootView.getContext();
    this.parent = (KiaraActivity) getActivity();

    // Use previous set of songs (the ones cached already) until the network call is returned.
    this.mAdapter = new SongListViewAdapter(new ArrayList<Song>(songs), mContext);
    Log.d(log, "Using previous set of songs until network call returned. ");

    mRecyclerView.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,
        new RecyclerItemTouchListener.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            Log.d(log, "Item clicked at position " + position);
            Intent i = new Intent(mContext, PlayerActivity.class);
            i.putExtra(PlayerFragment.SONG_PARAM, new SongParcelable(songs.get(position)));
            startActivity(i);
          }
        }));
    progressBar.setVisibility(View.INVISIBLE);

    setUpFab();

    return rootView;
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
        mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
        throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Subscribe
  public void onSongAdded(final SongAdded song) {
    mAdapter.addSong(song.getSong());
    Crouton.makeText(parent, "Added song.", Style.CONFIRM).show();
  }

  /*
   * Update the RecyclerView with new data as the network call has returned.
   */
  @Subscribe
  public void onSongsReceived(final ArrayList<Song> songs) {
    if(songs.size() > 0 && songs.get(0) instanceof Song) {
      Log.d(log, "onSongsReceived, updating the list.");
      this.songs.clear();
      for (Song s : songs) {
        this.songs.add(new SongParcelable(s));
      }
      this.mAdapter.updateSongs(songs);
    }
  }

  private void setUpFab() {
    Drawable plus = getResources().getDrawable(R.drawable.ic_add_white_24dp);
    FloatingActionButton fab = new FloatingActionButton.Builder(getActivity())
        .withButtonColor(getResources().getColor(R.color.pinkA200))
        .withDrawable(plus)
        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
        .withMargins(0, 0, 24, 24)
        .create();

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FragmentManager fm = getFragmentManager();
        AddSongDialog.newInstance(id).show(fm, "fragment_add_song");
      }
    });
    fab.showFloatingActionButton();
  }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
      // TODO: Update argument type and name
      public void onFragmentInteraction(Uri uri);
    }

}
