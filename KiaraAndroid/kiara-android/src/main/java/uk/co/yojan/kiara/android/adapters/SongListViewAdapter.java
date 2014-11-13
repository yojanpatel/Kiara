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
import uk.co.yojan.kiara.android.comparators.SongComparatorByArtist;
import uk.co.yojan.kiara.client.data.Song;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class SongListViewAdapter extends RecyclerView.Adapter<SongListViewAdapter.ViewHolder> {

  private List<Song> data;
  private Context mContext;
  private static Picasso picasso;

  public SongListViewAdapter(List<Song> songs, Context context) {
    this.data = songs;
    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(true);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.song_row, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    Song s = data.get(position);
    viewHolder.songName.setText(s.getSongName());
    viewHolder.artistName.setText(s.getArtistName());
    viewHolder.albumName.setText(s.getAlbumName());
    picasso.load(s.getImageURL())
        .placeholder(R.drawable.placeholder)
        .resize(150, 150)
        .into(viewHolder.albumArt);
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public void addSong(Song song) {
    data.add(song);
    Collections.sort(this.data, new SongComparatorByArtist());
    notifyDataSetChanged();
  }

  public void updateSongs(List<Song> songs) {
    this.data.clear();
    this.data.addAll(songs);
    Collections.sort(this.data, new SongComparatorByArtist());
    notifyDataSetChanged();
  }


  public static class ViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.song_img) ImageView albumArt;
    @InjectView(R.id.song_name) TextView songName;
    @InjectView(R.id.artist_name) TextView artistName;
    @InjectView(R.id.album_name) TextView albumName;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }
}
