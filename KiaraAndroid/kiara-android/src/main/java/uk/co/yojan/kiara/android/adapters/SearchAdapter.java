package uk.co.yojan.kiara.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.Utils;
import uk.co.yojan.kiara.android.utils.CircularCropTransformation;
import uk.co.yojan.kiara.android.views.MultiLevelExpIndListAdapter;
import uk.co.yojan.kiara.client.data.spotify.Album;
import uk.co.yojan.kiara.client.data.spotify.Artist;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class SearchAdapter extends MultiLevelExpIndListAdapter {


  public static final int VIEW_TRACK = 0;
  public static final int VIEW_ARTIST = 1;
  public static final int VIEW_ALBUM = 2;
  public static final int VIEW_LABEL = 3;
  public static final int VIEW_TRACK_CHECKED = 4;

  private int numTracks, numArtists, numAlbums;
  private int trackLabel, artistLabel, albumlabel;

  private SearchResult searchResult;

  private Context mContext;
  private Picasso picasso;

  // 8dp unit of padding
  private final int padding = 16;

  Typeface robotoMedium;

  /**
   * Array that represents the different components of the search results being displayed.
   * A blank Object() is used as a placeholder to represent labels.
   * Otherwise, a Track, Album or Artist object is placed.
   *
   * As various components are loaded, i.e. Albums for an artist, they are spliced into this
   * array. Therefore, all management of the views should be carried out directly on this array.
   *
   * Note: notifyDataChanged() must be called everytime the updates should propagate to the views.
   */
  ArrayList data;


  /**
   * A set of track ids that have been checked, to be added as a batch add event.
   */
  HashSet<String> toBeAdded;

  public static class ViewHolderTrack extends RecyclerView.ViewHolder {

    String id;
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

    public ViewHolderTrack id(String id) {
      this.id = id;
      return this;
    }
  }

  public static class ViewHolderTrackChecked extends RecyclerView.ViewHolder {

    String id;
    @InjectView(R.id.song_name) TextView songName;
    @InjectView(R.id.song_checked) CheckBox checked;

    public ViewHolderTrackChecked(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
    }

    public ViewHolderTrackChecked id(String id) {
      this.id = id;
      return this;
    }
  }


  public class ViewHolderArtist extends RecyclerView.ViewHolder {

    String id;
    @InjectView(R.id.artist_img) ImageView artistImg;
    @InjectView(R.id.artist_name) TextView artistName;

    public ViewHolderArtist(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);

      // setting the tag allows us to retrieve the viewholder later.
      itemView.setTag(this);
    }

    public ViewHolderArtist id(String id) {
      this.id = id;
      return this;
    }
  }


  public static class ViewHolderAlbum extends RecyclerView.ViewHolder {

    String id;
    @InjectView(R.id.album_img) ImageView albumImg;
    @InjectView(R.id.album_name) TextView albumName;

    public ViewHolderAlbum(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
    }

    public ViewHolderAlbum id(String id) {
      this.id = id;
      return this;
    }
  }

  public class ViewHolderLabel extends RecyclerView.ViewHolder {
    String id;
    @InjectView(R.id.label_text) TextView label;
    @InjectView(R.id.divider) View divider;

    public ViewHolderLabel(View itemView) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      itemView.setTag(this);
    }

    public ViewHolderLabel id(String id) {
      this.id = id;
      return this;
    }
  }


  /**
   * Constructor
   * @param context parent activity/fragment context
   * @param result the SearchResult this adapter is being used for
   */
  public SearchAdapter(Context context, SearchResult result) {
    mContext = context;
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
    data = new ArrayList();
    toBeAdded = new HashSet<String>();
    robotoMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");

    updateSearchResult(result);
  }

  public void updateSearchResult(SearchResult result) {
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

    data.clear();

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

    notifyDataSetChanged();
  }

  /** Create the view corresponding layout for each row. **/
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v;
    RecyclerView.ViewHolder viewHolder = null;
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    int resource;
    switch(viewType) {
      case VIEW_TRACK:
        resource = R.layout.song_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderTrack(v);
        break;
      case VIEW_TRACK_CHECKED:
        resource = R.layout.song_check_row;
        v = inflater.inflate(resource, parent, false);
        viewHolder = new ViewHolderTrackChecked(v);
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
        vht.id(track.getId());

        if(position < albumlabel && position < artistLabel) {
          if(track.getAlbum().getImages().size() > 0) {
            picasso.load(track.getAlbum().getImages().get(0).getUrl())
                .placeholder(R.drawable.ic_placeholder_200)
                .resize(150, 150)
                .into(vht.albumArt);
          }
        }
        break;

      case VIEW_TRACK_CHECKED:
        ViewHolderTrackChecked vhtc = (ViewHolderTrackChecked) viewHolder;
        Track trackc = (Track) data.get(position);
        vhtc.songName.setText(trackc.getName());
        vhtc.checked.setTag(trackc.getId());

        if(toBeAdded.contains(trackc.getId())) {
          vhtc.checked.setChecked(true);
        } else {
          vhtc.checked.setChecked(false);
        }
        vhtc.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String id = (String) buttonView.getTag();
            if(isChecked) {
              toBeAdded.add(id);
              Log.d("SearchAdapter", id + " checked");
            } else {
              toBeAdded.remove(id);
              Log.d("SearchAdapter", id + " unchecked");
            }
          }
        });
        break;

      case VIEW_ARTIST:
        ViewHolderArtist vhar = (ViewHolderArtist) viewHolder;
//        Artist artist = searchResult.getArtists().getArtists().get(position - artistLabel - 1);
        Artist artist = (Artist) data.get(position);
        vhar.artistName.setText(artist.getName());
        vhar.artistName.setTypeface(robotoMedium);
        vhar.id(artist.getId());

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
          vhal.itemView.setPadding(dpToPixels(2 * padding), dpToPixels(4), 0 , dpToPixels(4));
        } else {
          vhal.itemView.setPadding(dpToPixels(padding), dpToPixels(4), 0, dpToPixels(4));
        }

//        Album album = searchResult.getAlbums().getAlbums().get(position - albumlabel - 1);
        Album album = (Album) data.get(position);
        vhal.albumName.setText(album.getName());
        vhal.albumName.setTypeface(robotoMedium);
        vhal.id(album.getId());

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
        int visibility = View.VISIBLE;

        if(position == trackLabel) {
          text = "Tracks";
          visibility = View.GONE;
        } else if(position == artistLabel) {
          text = "Artists";
          visibility = View.VISIBLE;
        } else if(position == albumlabel) {
          text = "Albums";
          visibility = View.VISIBLE;
        }

        ((ViewHolderLabel)viewHolder).divider.setVisibility(visibility);
        vhl.label.setText(text);
        vhl.id(text);
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
    if (o instanceof Track) {
      // if there are singleton tracks and position is between the track label and artist/album label.
      if(trackLabel >= 0 &&
          ((artistLabel >= 0 && trackLabel < position && position < artistLabel) ||
           (artistLabel < 0 && albumlabel >= 0 && trackLabel < position && position < albumlabel))) {
        return VIEW_TRACK;
      } else {
        return VIEW_TRACK_CHECKED;
      }
    }
    else if (o instanceof Artist) return VIEW_ARTIST;
    else if (o instanceof Album) return VIEW_ALBUM;
    else if (o != null) return VIEW_LABEL;
    else return -1;
  }


  public Track track(int position) {
    Object o = data.get(position);
    if(o != null) return (Track) o;
    return null;
  }

  public Artist artist(int position) {
    Object o =  data.get(position);
    if (o != null) return (Artist) o;
    else return null;
  }

  public Album album(int position) {
    Object o =  data.get(position);
    if (o != null) return (Album) o;
    else return null;
  }

  public void closeArtist(int position) {
    int offset = 0;
    while(!(data.get(position + offset + 1) instanceof Artist)) {
      offset++;
    }

    // clear the subrange of the arraylist corresponding to the open views
    data.subList(position + 1, position + offset + 1).clear();


    if(offset > 0) {
      notifyItemRangeRemoved(position + 1, offset);
    }

    // move the album label offset again
    albumlabel -= offset;
  }

  public void closeAlbum(int position) {
    int offset = 0;
    while(!(data.get(position + offset + 1) instanceof Album)) {
      offset++;
    }

    // clear the subrange of the arraylist
    data.subList(position + 1, position + offset + 1).clear();

    if(offset > 0) {
      notifyItemRangeRemoved(position + 1, offset);
    }

    if(position < albumlabel) {
      albumlabel -= offset;
    }
  }

  /**
   * A certain row is expanded if the next view is not of the same type.
   * e.g. If an album follows an artist view, the artist view must be expanded.
   * Similarly, if a track follows an album.
   *
   * Check for label has to be made. A view cannot be expanded if the next view is a label.
  **/
  public boolean isExpanded(int position) {
    int nextPosition = position + 1;
    if(nextPosition == artistLabel || nextPosition == albumlabel) {
      return false;
    }

    if(position < data.size())
      return !data.get(position).getClass().equals(data.get(position + 1).getClass());
    else
      return false;
  }


  public void addAlbums(int positionClicked, List<Album> albums) {
    data.addAll(positionClicked + 1, albums);
    Log.d("BUS", "ADDALBUMS");

    // move the album label up if it exists.
    if(albumlabel >= 0) {
      albumlabel += albums.size();
    }
    notifyItemRangeInserted(positionClicked + 1, albums.size());
  }

  public void addTracks(int positionClicked, List<Track> tracks) {
    data.addAll(positionClicked + 1, tracks);

    // update album label to compensate for added tracks if they were added in the artist section.
    if(positionClicked < albumlabel) {
      if(albumlabel >= 0) {
        albumlabel += tracks.size();
      }
    }
    notifyItemRangeInserted(positionClicked + 1, tracks.size());
  }

  private int dpToPixels(float dp) {
    return Utils.dpToPixels(mContext, dp);
  }
}
