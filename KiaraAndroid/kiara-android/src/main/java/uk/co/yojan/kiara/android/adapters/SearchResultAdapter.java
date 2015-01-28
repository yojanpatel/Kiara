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

  public static final int TRACK_TYPE = 0;
  public static final int ARTIST_TYPE = 1;
  public static final int ALBUM_TYPE = 2;
  public static final int LABEL_TYPE = 3;


  private SearchResult data;
  private int numTracks, numArtists, numAlbums;
  private int trackLabel, artistLabel, albumlabel;

  private Context mContext;
  private static Picasso picasso;

  public SearchResultAdapter(SearchResult result, Context context) {
    this.data = result;
    this.numTracks = result.getTracks().getTracks().size();
    this.numArtists = result.getArtists().getArtists().size();
    this.numAlbums = result.getAlbums().getAlbums().size();

    trackLabel = numTracks > 0 ? 0 : -1;
    artistLabel = numArtists > 0
        ? numTracks + (numTracks > 0 ? 1 : 0)
        : -1;
    albumlabel = numAlbums > 0
        ? (numArtists > 0) ? artistLabel + numArtists + (numTracks > 0 ? 1 : 0)
            : (numTracks > 0) ? numTracks
                : 0
        : -1;

    Log.d("SearchResultAdapter", numTracks + " " + numArtists + " " + numAlbums + " " + getItemCount());
    Log.d("SearchResultAdapter", "labels at " + trackLabel + " " + artistLabel + " " + albumlabel);

    this.mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
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
      } else if(viewType == LABEL_TYPE) {
        return new ViewHolderLabel(inflater.inflate(R.layout.label_row, parent, false));
      }
      else return null;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    int viewType = getItemViewType(position);
    Log.d("SearchResultAdapter", viewType + " " + position);
    if(viewType == TRACK_TYPE) {
      Log.d("SearchResultAdapter", "Casting to ViewHolderTrack");
      ViewHolderTrack vhTrack = (ViewHolderTrack) viewHolder;
      Track track = data.getTracks().getTracks().get(position - 1);
      vhTrack.songName.setText(track.getName());
      vhTrack.artistName.setText(track.getArtists().get(0).getName());
      if(track.getAlbum().getImages().size() > 0) {
        picasso.load(track.getAlbum().getImages().get(0).getUrl())
            .placeholder(R.drawable.ic_placeholder_200)
            .resize(150, 150)
            .into(vhTrack.albumArt);
      }

    } else if(viewType == ARTIST_TYPE) {
      Log.d("SearchResultAdapter", "Casting to ViewHolderArtist");
      ViewHolderArtist vhArtist = (ViewHolderArtist) viewHolder;
      Artist artist = data.getArtists().getArtists().get(position - artistLabel - 1);
      vhArtist.artistName.setText(artist.getName());
      boolean artistImage = artist.getImages().size() > 0 && !artist.getImages().get(0).getUrl().isEmpty();

      // WHERE IS DYNAMIC TYPING?!? - Ew.
      if(artistImage) {
        picasso.load(artist.getImages().get(0).getUrl())
            .placeholder(R.drawable.ic_placeholder_150)
            .resize(150, 150)
            .transform(new CircularCropTransformation())
            .into(vhArtist.artistImg);
      } else {
        picasso.load(R.drawable.ic_placeholder_150)
            .placeholder(R.drawable.ic_placeholder_150)
            .resize(150, 150)
            .transform(new CircularCropTransformation())
            .into(vhArtist.artistImg);
      }
    } else if(viewType == ALBUM_TYPE) {
      ViewHolderAlbum vhAlbum = (ViewHolderAlbum) viewHolder;
      Album album = data.getAlbums().getAlbums().get(position - albumlabel - 1);
      vhAlbum.albumName.setText(album.getName());
//      vhAlbum.artistName.setText(album.getArtists().getArtists().get(0).getName());
      if(album.getImages().size() > 0) {
        picasso.load(album.getImages().get(0).getUrl())
            .placeholder(R.drawable.ic_placeholder_200)
            .resize(200, 200)
            .into(vhAlbum.albumImg);
      }
    } else if(viewType == LABEL_TYPE) {
      ViewHolderLabel vhLabel = (ViewHolderLabel) viewHolder;
      String text = "";
      if(position == trackLabel) {
        text = "Tracks";
        ((ViewHolderLabel)viewHolder).divider.setVisibility(View.GONE);
      }
      else if(position == artistLabel) text = "Artists";
      else if(position == albumlabel) text = "Albums";
      vhLabel.label.setText(text);
    }
  }

  @Override
  public int getItemCount() {
    int count = numTracks + numArtists + numAlbums;
    if(numTracks > 0) count++;  // track label
    if(numArtists > 0) count++; // artist label
    if(numAlbums > 0) count++;  // album label
    return count;
  }

  @Override
  public int getItemViewType(int position) {
    if(position == trackLabel || position == artistLabel || position == albumlabel) return LABEL_TYPE;
    else if(trackLabel >= 0 && position < numTracks + 1) return TRACK_TYPE;
    else if(artistLabel >= 0 && position < artistLabel + numArtists + 1) return ARTIST_TYPE;
    else if(albumlabel >= 0 && position < albumlabel + numAlbums + 1) return ALBUM_TYPE;
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
      divider.setVisibility(View.GONE);
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
    }
  }

  public class ViewHolderLabel extends RecyclerView.ViewHolder {
    @InjectView(R.id.label_text) TextView label;
    @InjectView(R.id.divider) View divider;

    public ViewHolderLabel(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
    }
  }
}
