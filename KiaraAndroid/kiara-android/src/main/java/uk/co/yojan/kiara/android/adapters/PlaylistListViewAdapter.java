package uk.co.yojan.kiara.android.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.utils.PaletteTransformation;
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

  private static final boolean ALT_VIEW = true;

  private List<PlaylistWithSongs> data;
  private Context mContext;
  private Resources res;

  private int width;
  private int height;

  private static Picasso picasso;

  public PlaylistListViewAdapter(Context context) {
    this.data = new ArrayList<PlaylistWithSongs>();
    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
  }

  public PlaylistListViewAdapter(Context context, int width, int height) {
    this.data = new ArrayList<PlaylistWithSongs>();
    this.mContext = context;
    this.res = mContext.getResources();

    this.width = width;
    this.height = height;

    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
  }

  // Create a new view. This method is invoked by the layout manager.
  @Override
  public PlaylistListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v;
    if(ALT_VIEW) {
      v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.playlist_alt_row, parent, false);

      v.setMinimumHeight(width / 2);
      return new ViewHolder(v, width, height);
    } else {
      v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.playlist_row, parent, false);
      return new ViewHolder(v);
    }
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, int position) {
    PlaylistWithSongs pws = data.get(position);
    Playlist p = pws.getPlaylist();

    viewHolder.playlistName.setText(p.getPlaylistName());
    int numSongs = pws.getSongs().size();

    if(numSongs > 0) {
      picasso.load(pws.getSongs().get(0).getImageURL())
          .placeholder(R.drawable.ic_placeholder_200)
          .resize(width / 2, width / 2)
          .transform(PaletteTransformation.instance())
          .into(viewHolder.image, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
              Bitmap bitmap = ((BitmapDrawable) viewHolder.image.getDrawable()).getBitmap(); // Ew!
              Palette palette = PaletteTransformation.getPalette(bitmap);
              changeUi(viewHolder, palette);
            }
          });

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
      if(ALT_VIEW) {
        picasso.load(R.drawable.ic_placeholder_200)
            .resize(width / 2, width / 2)
            .transform(PaletteTransformation.instance())
            .into(viewHolder.image, new Callback.EmptyCallback() {
              @Override
              public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) viewHolder.image.getDrawable()).getBitmap(); // Ew!
                Palette palette = PaletteTransformation.getPalette(bitmap);
                changeUi(viewHolder, palette);
              }
            });
      } else {
        picasso.load(R.drawable.ic_placeholder_200).resize(200, 200).into(viewHolder.image);
      }
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

  private void changeUi(ViewHolder viewHolder, Palette palette) {
    viewHolder.playlistName.setTextColor(palette.getLightVibrantColor(res.getColor(R.color.grey50)));
    viewHolder.itemView.findViewById(R.id.ll).findViewById(R.id.rl).setBackgroundColor(palette.getDarkMutedColor(res.getColor(R.color.grey900)));
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.playlist_row_img) ImageView image;
    @InjectView(R.id.playlist_row_name) TextView playlistName;
    @InjectView(R.id.playlist_row_details) TextView details;
    @InjectView(R.id.playlist_row_size) TextView size;

    int width, height;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }

    public ViewHolder(View itemView, int width, int height) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      this.width = width;
      this.height = height;
      image.setAdjustViewBounds(true);
      image.setMaxHeight(width/2);
      image.setMinimumHeight(width/2);
      image.setMaxWidth(width/2);
      image.setMinimumWidth(width/2);
    }
  }
}
