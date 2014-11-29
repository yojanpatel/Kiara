package uk.co.yojan.kiara.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.apache.commons.lang.StringUtils;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.client.data.spotify.PlaylistTracks;

import java.util.List;

/**
 * Adapter to populate list with simple row elements for each track in a playlist.
 */
public class FilterTracksAdapter extends RecyclerView.Adapter<FilterTracksAdapter.ViewHolder> {

  List<PlaylistTracks.PlaylistTrack> tracks;
  Context context;

  public FilterTracksAdapter(PlaylistTracks playlistTracks, Context context) {
    tracks = playlistTracks.getTracks();
  }

  @Override
  public FilterTracksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.song_row_simple, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    PlaylistTracks.PlaylistTrack track = tracks.get(position);

    viewHolder.title.setText(track.getTrackName());

    StringBuilder sb = new StringBuilder();
    String artistName = track.getArtistName();
    String albumName = track.getAlbumName();
    sb.append(artistName);
    if(!StringUtils.isBlank(artistName) &&
       !StringUtils.isBlank(albumName)) {
      sb.append(" - ");
    }
    sb.append(albumName);
    viewHolder.detail.setText(sb.toString());
  }

  @Override
  public int getItemCount() {
    return tracks.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.detail) TextView detail;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }
}
