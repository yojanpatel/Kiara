package uk.co.yojan.kiara.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.SearchResultAdapter;
import uk.co.yojan.kiara.android.events.AddSong;
import uk.co.yojan.kiara.android.events.SearchRequest;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.List;

public class AddSongDialog extends DialogFragment {

  Context mContext;
  KiaraActivity activity;

  @InjectView(R.id.search_query_edit) EditText queryEdit;
  @InjectView(R.id.search_btn_dialog) ImageButton searchBtn;
  @InjectView(R.id.search_result_text) TextView results;

  @InjectView(R.id.results_list) RecyclerView resultList;
  @InjectView(R.id.progressBar) ProgressBar progressBar;

  private long id;
  private Crouton searchCrouton;
  private SearchResultAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private SearchResult result;

  public static AddSongDialog newInstance(long id) {
    AddSongDialog asd = new AddSongDialog();
    asd.setId(id);
    return asd;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    this.activity = (KiaraActivity)getActivity();
    this.mContext = getActivity().getApplicationContext();

    View view = activity.getLayoutInflater().inflate(R.layout.search_song_dialog, null);
    ButterKnife.inject(this, view);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setView(view)
        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            String query = queryEdit.getText().toString();
            Crouton.makeText(getActivity(), "Searching for " + query, Style.INFO).show();
            ((KiaraActivity) getActivity()).getBus()
                .post(new SearchRequest(query, id, 0, 2));
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // User cancelled the dialog, do nothing.
            dialogInterface.cancel();
          }
        });

    mLayoutManager = new LinearLayoutManager(activity);
    resultList.setLayoutManager(mLayoutManager);

    searchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        searchClick(v);
      }
    });
    return builder.create();
  }

  public void searchClick(View view) {
    String query = queryEdit.getText().toString();

    Crouton.cancelAllCroutons();
    searchCrouton = Crouton.makeText(getActivity(), "Searching for " + query, Style.INFO, (ViewGroup)getView());
    searchCrouton.show();
    progressBar.setVisibility(View.VISIBLE);
    resultList.setVisibility(View.INVISIBLE);

    activity.getBus().post(new SearchRequest(query, id, 0, 3));
  }

  /**
   * Called when the fragment is visible to the user and actively running.
   * This is generally
   * tied to {@link android.app.Activity#onResume() Activity.onResume} of the containing
   * Activity's lifecycle.
   */
  @Override
  public void onResume() {
    super.onResume();
    activity.getBus().register(this);
  }

  // Maximise dialog to take up the entire screen.
  public void enlarge() {
    int width = getResources().getDisplayMetrics().widthPixels;
    int height = getResources().getDisplayMetrics().heightPixels;
    getDialog().getWindow().setLayout(width, height);
  }

  /**
   * Called when the Fragment is no longer resumed.  This is generally
   * tied to {@link android.app.Activity#onPause() Activity.onPause} of the containing
   * Activity's lifecycle.
   */
  @Override
  public void onPause() {
    super.onPause();
    activity.getBus().unregister(this);
  }

  @Subscribe
  public void onSearchResultsReceived(final SearchResult result) {
    Log.d("AddSongDialog", "search results received.");
    enlarge();
    Crouton.hide(searchCrouton);
    progressBar.setVisibility(View.INVISIBLE);
    resultList.setVisibility(View.VISIBLE);

    this.result = result;
    this.mAdapter = new SearchResultAdapter(result, mContext);
    resultList.setAdapter(mAdapter);
    resultList.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,
        new RecyclerItemTouchListener.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            List<Track> tracks = AddSongDialog.this.result.getTracks().getTracks();
            if(mAdapter.getItemViewType(position) == SearchResultAdapter.TRACK_TYPE) {
              Track track = tracks.get(position - 1);
              activity.getBus().post(new AddSong(id, track.getId()));
              getDialog().dismiss();
              Log.d("results", track.getName());
              Crouton.makeText(getActivity(), "Adding " + track.getName(), Style.INFO, (ViewGroup)getView()).show();
            }
          }
        }));
  }

  private void setId(long id) {
    this.id = id;
  }
}
