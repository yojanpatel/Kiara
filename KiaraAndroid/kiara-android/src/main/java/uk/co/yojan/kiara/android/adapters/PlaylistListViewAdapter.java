package uk.co.yojan.kiara.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlaylistListFragment;
import uk.co.yojan.kiara.android.utils.CircularCropTransformation;
import uk.co.yojan.kiara.client.data.Playlist;

import java.util.List;

/**
 * An adapter for displaying playlists in a list format using the RecyclerView Api.
 * This class is responsible for providing the views that represent the playlist.
 */
public class PlaylistListViewAdapter extends RecyclerView.Adapter<PlaylistListViewAdapter.ViewHolder> {

  private List<Playlist> data;
  private Context mContext;

  private static Picasso picasso;

  public PlaylistListViewAdapter(List<Playlist> playlists, Context context) {
    this.data = playlists;
    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(true);
  }

  // Create a new view. This method is invoked by the layout manager.
  @Override
  public PlaylistListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.playlist_row, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }


  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    Playlist p = data.get(position);
    viewHolder.playlistName.setText(p.getPlaylistName());
    viewHolder.details.setText("Bonobo, Flying Lotus, Pearl ...");
    picasso.load("http://blog.iso50.com/wp-content/uploads/2010/03/Bonobo_10-450x450.png")
           .placeholder(android.R.drawable.btn_star_big_on)
           .resize(200, 200)
           .transform(new CircularCropTransformation())
           .into(viewHolder.image);
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.playlist_row_img) ImageView image;
    @InjectView(R.id.playlist_row_name) TextView playlistName;
    @InjectView(R.id.playlist_row_details) TextView details;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }

  public void updateList(List<Playlist> newData) {
    if(newData != null) {
      data = newData;
      notifyDataSetChanged();
    }
  }
}
