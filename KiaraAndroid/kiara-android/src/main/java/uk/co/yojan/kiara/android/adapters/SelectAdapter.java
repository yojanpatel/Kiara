package uk.co.yojan.kiara.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.BrowseTabActivity;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.*;


public class SelectAdapter extends RecyclerView.Adapter {


  List<Track> tracks;


  // acts as a context, activity and an OnSelectionChangeListener
  private BrowseTabActivity activity;
  private HashSet<Track> selected;

  int lightgrey;

  public SelectAdapter(BrowseTabActivity activity) {

    tracks = new ArrayList<Track>();
    this.activity = activity;
    this.selected = activity.selectedSongs();
    for(Track t : selected) {
      tracks.add(t);
    }
    Collections.sort(tracks, new Comparator<Track>() {
      @Override
      public int compare(Track lhs, Track rhs) {
        return lhs.getArtists().get(0).getName().compareTo(rhs.getArtists().get(0).getName());
      }
    });

    lightgrey = activity.getResources().getColor(R.color.grey50);

  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_check_row, viewGroup, false));
  }


  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
    Track track = tracks.get(position);
    ViewHolder vhtc = (ViewHolder) vh;

    vhtc.songName.setText(track.getName());
    vhtc.artistName.setText(track.getArtists().get(0).getName());
    vhtc.checked.setTag(track);
    vhtc.songName.setTextColor(lightgrey);

    if(selected.contains(track)) {
      vhtc.checked.setChecked(true);
    } else {
      vhtc.checked.setChecked(false);
    }

    vhtc.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Track track = (Track) buttonView.getTag();
        if(isChecked) {
          selected.add(track);
        } else {
          selected.remove(track);
        }

        activity.onSongSelectionChanged(selected);
      }
    });
  }

  @Override
  public int getItemCount() {
    if(tracks == null) return 0;
    return tracks.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    String id;
    @InjectView(R.id.song_name) TextView songName;
    @InjectView(R.id.artist_name) TextView artistName;
    @InjectView(R.id.song_checked) CheckBox checked;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
      artistName.setVisibility(View.VISIBLE);
    }

    public ViewHolder id(String id) {
      this.id = id;
      return this;
    }
  }
}
