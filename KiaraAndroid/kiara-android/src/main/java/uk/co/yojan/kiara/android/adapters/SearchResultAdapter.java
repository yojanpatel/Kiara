package uk.co.yojan.kiara.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.utils.CircularCropTransformation;
import uk.co.yojan.kiara.client.data.spotify.Album;
import uk.co.yojan.kiara.client.data.spotify.Artist;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.Track;


public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TRACK_TYPE = 0;
  private static final int ARTIST_TYPE = 1;
  private static final int ALBUM_TYPE = 2;


  private SearchResult data;
  private int numTracks, numArtists, numAlbums;

  private Context mContext;
  private static Picasso picasso;

  public SearchResultAdapter(SearchResult result, Context context) {
    this.data = result;
    this.numTracks = result.getTracks().getTracks().size();
    this.numArtists = result.getArtists().getArtists().size();
    this.numAlbums = result.getAlbums().getAlbums().size();
    Log.d("SearchResultAdapter", numTracks + " " + numArtists + " " + numAlbums + " " + getItemCount());
    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(true);
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater =  LayoutInflater.from(parent.getContext());

      if(viewType == TRACK_TYPE) {
        return new ViewHolderTrack(inflater.inflate(R.layout.song_row, parent, false));
      } else if(viewType == ARTIST_TYPE) {
          return new ViewHolderArtist(inflater.inflate(R.layout.artist_row, parent, false));
      } else if(viewType == ALBUM_TYPE) {
        return new ViewHolderAlbum(inflater.inflate(R.layout.album_row, parent, false));
      }
      else return null;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    int viewType = getItemViewType(position);
    Log.d("SearchResultAdapter", getItemViewType(5) + " " + 5);
    Log.d("SearchResultAdapter", viewType + " " + position);
    if(viewType == TRACK_TYPE) {
      Log.d("SearchResultAdapter", "Casting to ViewHolderTrack");
      ViewHolderTrack vhTrack = (ViewHolderTrack) viewHolder;
      Track track = data.getTracks().getTracks().get(position);
      vhTrack.songName.setText(track.getName());
      vhTrack.artistName.setText(track.getArtists().get(0).getName());
      vhTrack.albumName.setText(track.getAlbum().getName());
      if(track.getAlbum().getImages().size() > 0) {
        picasso.load(track.getAlbum().getImages().get(0).getUrl())
            .placeholder(R.drawable.placeholder)
            .resize(200, 200)
            .into(vhTrack.albumArt);
      }

    } else if(viewType == ARTIST_TYPE) {
      Log.d("SearchResultAdapter", "Casting to ViewHolderArtist");
      ViewHolderArtist vhArtist = (ViewHolderArtist) viewHolder;
      Artist artist = data.getArtists().getArtists().get(position - numTracks);
      vhArtist.artistName.setText(artist.getName());
      if(artist.getImages().size() > 0) {
        picasso.load(artist.getImages().get(0).getUrl())
            .placeholder(R.drawable.placeholder)
            .resize(200, 200)
            .transform(new CircularCropTransformation())
            .into(vhArtist.artistImg);
      }

    } else if(viewType == ALBUM_TYPE) {
      ViewHolderAlbum vhAlbum = (ViewHolderAlbum) viewHolder;
      Album album = data.getAlbums().getAlbums().get(position - numArtists - numTracks);
      vhAlbum.albumName.setText(album.getName());
//      vhAlbum.artistName.setText(album.getArtists().getArtists().get(0).getName());
      if(album.getImages().size() > 0) {
        picasso.load(album.getImages().get(0).getUrl())
            .placeholder(R.color.indigo100)
            .resize(200, 200)
            .into(vhAlbum.albumImg);
      }
    }
  }

  @Override
  public int getItemCount() {
    return numTracks + numArtists + numAlbums;
  }

  @Override
  public int getItemViewType(int position) {
    if(position < numTracks) return TRACK_TYPE;
    else if(position < numTracks + numArtists) return ARTIST_TYPE;
    else if(position < numTracks + numArtists + numAlbums) return ALBUM_TYPE;
    else return -1;
  }


  public class ViewHolderTrack extends RecyclerView.ViewHolder {

    @InjectView(R.id.song_img) ImageView albumArt;
    @InjectView(R.id.song_name) TextView songName;
    @InjectView(R.id.artist_name) TextView artistName;
    @InjectView(R.id.album_name) TextView albumName;
    @InjectView(R.id.divider) View divider;

    public ViewHolderTrack(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      songName.setTextColor(mContext.getResources().getColor(R.color.grey900));
      artistName.setTextColor(mContext.getResources().getColor(R.color.grey900));
      albumName.setTextColor(mContext.getResources().getColor(R.color.grey900));
      divider.setVisibility(View.VISIBLE);
    }
  }


  public class ViewHolderArtist extends RecyclerView.ViewHolder {

    @InjectView(R.id.artist_img) ImageView artistImg;
    @InjectView(R.id.artist_name) TextView artistName;

    public ViewHolderArtist(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }


  public class ViewHolderAlbum extends RecyclerView.ViewHolder {

    @InjectView(R.id.album_img) ImageView albumImg;
    @InjectView(R.id.album_name) TextView albumName;
    @InjectView(R.id.artist_name) TextView artistName;

    public ViewHolderAlbum(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      albumName.setTextColor(mContext.getResources().getColor(R.color.grey900));
      artistName.setTextColor(mContext.getResources().getColor(R.color.grey900));
    }
  }
}
