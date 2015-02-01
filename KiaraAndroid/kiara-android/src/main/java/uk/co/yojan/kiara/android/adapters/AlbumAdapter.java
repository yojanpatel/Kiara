package uk.co.yojan.kiara.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.client.data.spotify.Album;

import java.util.List;


public class AlbumAdapter extends RecyclerView.Adapter {


  List<Album> albums;
  Picasso picasso;

  public AlbumAdapter(List<Album> albums, Picasso picasso) {
    this.albums = albums;
    this.picasso = picasso;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    return new SearchAdapter.ViewHolderAlbum(
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album_row, viewGroup, false));
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    Album album = albums.get(position);
    SearchAdapter.ViewHolderAlbum vha = (SearchAdapter.ViewHolderAlbum) viewHolder;

    vha.albumName.setText(album.getName());
    vha.artistName.setText(album.getArtists().getArtists().get(0).getName());
    picasso.load(album.getImages().get(0).getUrl()).into(vha.albumImg);
  }

  @Override
  public int getItemCount() {
    return albums.size();
  }
}
