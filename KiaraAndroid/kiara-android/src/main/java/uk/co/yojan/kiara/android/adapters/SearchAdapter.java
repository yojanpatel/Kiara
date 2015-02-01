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
import uk.co.yojan.kiara.android.utils.CircularCropTransformation;
import uk.co.yojan.kiara.android.views.MultiLevelExpIndListAdapter;
import uk.co.yojan.kiara.client.data.spotify.Album;
import uk.co.yojan.kiara.client.data.spotify.Artist;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;
import java.util.List;


public class SearchAdapter extends MultiLevelExpIndListAdapter {


  public static final int VIEW_TRACK = 0;
  public static final int VIEW_ARTIST = 1;
  public static final int VIEW_ALBUM = 2;
  public static final int VIEW_LABEL = 3;

  private int numTracks, numArtists, numAlbums;
  private int trackLabel, artistLabel, albumlabel;

  private SearchResult searchResult;

  private Context mContext;
  private Picasso picasso;

  // 8dp unit of padding
  private final int padding = 8;


  ArrayList data;

  public static class ViewHolderTrack extends RecyclerView.ViewHolder {

    @InjectView(R.id.song_img) ImageView albumArt;
    @InjectView(R.id.song_name) TextView songName;
    @InjectView(R.id.artist_name) TextView artistName;
    @InjectView(R.id.album_name) TextView albumName;
    @InjectView(R.id.divider) View divider;

    public ViewHolderTrack(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      divider.setVisibility(View.GONE);
      itemView.setTag(this);
    }
  }


  public class ViewHolderArtist extends RecyclerView.ViewHolder {

    @InjectView(R.id.artist_img) ImageView artistImg;
    @InjectView(R.id.artist_name) TextView artistName;

    public ViewHolderArtist(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
    }
  }


  public static class ViewHolderAlbum extends RecyclerView.ViewHolder {

    @InjectView(R.id.album_img) ImageView albumImg;
    @InjectView(R.id.album_name) TextView albumName;
    @InjectView(R.id.artist_name) TextView artistName;

    public ViewHolderAlbum(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
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



  public SearchAdapter(Context context, SearchResult result) {
    mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
    data = new ArrayList();
    updateSearchResult(result);
  }

  private void updateSearchResult(SearchResult result) {
    this.searchResult = result;
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


    // represents a track label
    if(numTracks > 0) data.add(new Object());
    for(Track t : result.getTracks().getTracks()) {
      data.add(t);
    }

    // represents an artist label
    if(numArtists > 0) data.add(new Object());
    for(Artist a : result.getArtists().getArtists()) {
      data.add(a);
    }

    // represents an album label
    if(numAlbums > 0) data.add(new Object());
    for(Album a : result.getAlbums().getAlbums()) {
      data.add(a);
    }
  }

  /** Create the view corresponding layout for each row. **/
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v;
    RecyclerView.ViewHolder viewHolder = null;
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    int resource = -1;
    switch(viewType) {
      case VIEW_TRACK:
        resource = R.layout.song_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderTrack(v);
        break;
      case VIEW_ARTIST:
        resource = R.layout.artist_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderArtist(v);
        break;
      case VIEW_ALBUM:
        resource = R.layout.album_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderAlbum(v);
        break;
      case VIEW_LABEL:
        resource = R.layout.label_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderLabel(v);
        break;
    }

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    int viewType = getItemViewType(position);
    switch(viewType) {

      case VIEW_TRACK:
        ViewHolderTrack vht = (ViewHolderTrack) viewHolder;
//        Track track = searchResult.getTracks().getTracks().get(position - 1);
        Track track = (Track) data.get(position);
        vht.songName.setText(track.getName());
        vht.artistName.setText(track.getArtists().get(0).getName());
        if(track.getAlbum().getImages().size() > 0) {
          picasso.load(track.getAlbum().getImages().get(0).getUrl())
              .placeholder(R.drawable.ic_placeholder_200)
              .resize(150, 150)
              .into(vht.albumArt);
        }
        break;

      case VIEW_ARTIST:
        ViewHolderArtist vhar = (ViewHolderArtist) viewHolder;
//        Artist artist = searchResult.getArtists().getArtists().get(position - artistLabel - 1);
        Artist artist = (Artist) data.get(position);
        vhar.artistName.setText(artist.getName());
        boolean artistImage = artist.getImages().size() > 0 && !artist.getImages().get(0).getUrl().isEmpty();

        // WHERE IS DYNAMIC TYPING?!? - Ew.
        if(artistImage) {
          picasso.load(artist.getImages().get(0).getUrl())
              .placeholder(R.drawable.ic_placeholder_150)
              .resize(150, 150)
              .transform(new CircularCropTransformation())
              .into(vhar.artistImg);
        } else {
          picasso.load(R.drawable.ic_placeholder_150)
              .placeholder(R.drawable.ic_placeholder_150)
              .resize(150, 150)
              .transform(new CircularCropTransformation())
              .into(vhar.artistImg);
        }
        break;

      case VIEW_ALBUM:
        ViewHolderAlbum vhal = (ViewHolderAlbum) viewHolder;
        int imageSize = position < albumlabel ? 150 : 200;
        if(position < albumlabel) {
          vhal.itemView.setPadding(32, 0, 0 , 0);
        }

//        Album album = searchResult.getAlbums().getAlbums().get(position - albumlabel - 1);
        Album album = (Album) data.get(position);
        vhal.albumName.setText(album.getName());

        if(album.getArtists() != null && !album.getArtists().getArtists().isEmpty())
          vhal.artistName.setText(album.getArtists().getArtists().get(0).getName());
        else
          vhal.artistName.setText("");

        if(album.getImages().size() > 0) {
          picasso.load(album.getImages().get(0).getUrl())
              .placeholder(R.drawable.ic_placeholder_200)
              .resize(imageSize, imageSize)
              .into(vhal.albumImg);
        }
        break;

      case VIEW_LABEL:
        ViewHolderLabel vhl = (ViewHolderLabel) viewHolder;
        String text = "";
        if(position == trackLabel) {
          text = "Tracks";
          ((ViewHolderLabel)viewHolder).divider.setVisibility(View.GONE);
        }
        else if(position == artistLabel) text = "Artists";
        else if(position == albumlabel) text = "Albums";
        vhl.label.setText(text);
        break;
    }
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  @Override
  public int getItemViewType(int position) {

    Object o = data.get(position);
    if (o instanceof Track) return VIEW_TRACK;
    else if (o instanceof Artist) return VIEW_ARTIST;
    else if (o instanceof Album) return VIEW_ALBUM;
    else if (o != null) return VIEW_LABEL;
    else return -1;
  }


  public Artist artist(int position) {
    Object o =  data.get(position);
    if (o != null) return (Artist) o;
    else return null;
  }

  public void addAlbums(int positionClicked, List<Album> albums) {
    data.addAll(positionClicked + 1, albums);

    // move the album label up if it exists.
    if(albumlabel >= 0) {
      albumlabel += albums.size();
    }
    notifyDataSetChanged();
  }

  public void addTracks(int positionClicked, List<Track> tracks) {
    data.addAll(positionClicked + 1, tracks);

    // update album label to compensate for added tracks if they were added in the artist section.
    if(positionClicked < albumlabel) {
      if(albumlabel >= 0) {
        albumlabel += tracks.size();
      }
    }
    notifyDataSetChanged();
  }
}
