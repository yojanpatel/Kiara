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
import uk.co.yojan.kiara.client.data.Playlist;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for displaying playlists in a list format using the RecyclerView Api.
 * This class is responsible for providing the views that represent the playlist.
 */
public class PlaylistListViewAdapter
    extends RecyclerView.Adapter<PlaylistListViewAdapter.ViewHolder>  {

  private static final String log = PlaylistListViewAdapter.class.getName();

  private List<PlaylistWithSongs> data;
  private Context mContext;

  private static Picasso picasso;

  public PlaylistListViewAdapter(Context context) {
    this.data = new ArrayList<PlaylistWithSongs>();
    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
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
    PlaylistWithSongs pws = data.get(position);
    Playlist p = pws.getPlaylist();

    viewHolder.playlistName.setText(p.getPlaylistName());
    int numSongs = pws.getSongs().size();

    if(numSongs > 0) {
      picasso.load(pws.getSongs().get(0).getImageURL())
          .placeholder(R.drawable.ic_placeholder_200)
          .resize(200, 200)
              //           .transform(new CircularCropTransformation())
          .into(viewHolder.image);
      StringBuffer detailText = new StringBuffer();
      for(int i = 0; i < Math.min(5, numSongs); i++) {
        detailText.append(pws.getSongs().get(i).getArtistName() + ", ");
      }
      detailText.delete(detailText.length() - 2, detailText.length() - 1);
      viewHolder.details.setText(detailText.toString());

      String sizeText = numSongs + (numSongs > 1 ? " songs." : " song.");
      viewHolder.size.setText(sizeText);
    } else {
      viewHolder.details.setText("No songs.");
      viewHolder.size.setText("");
      picasso.load(R.drawable.ic_placeholder_200).resize(200,200).into(viewHolder.image);
    }
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public void updateList(ArrayList<PlaylistWithSongs> newData) {
    if (newData != null) {
      data.clear();
      data.addAll(newData);
      notifyDataSetChanged();
    }
  }

  public void addPlaylist(Playlist p) {
    data.add(new PlaylistWithSongs(p, new ArrayList<Song>()));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.playlist_row_img) ImageView image;
    @InjectView(R.id.playlist_row_name) TextView playlistName;
    @InjectView(R.id.playlist_row_details) TextView details;
    @InjectView(R.id.playlist_row_size) TextView size;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }
}
