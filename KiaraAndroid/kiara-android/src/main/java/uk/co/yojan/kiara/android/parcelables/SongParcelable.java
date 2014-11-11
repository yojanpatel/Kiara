package uk.co.yojan.kiara.android.parcelables;

import android.os.Parcel;
import android.os.Parcelable;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * A parcelable version of Song for android data-flow passing.
 */
public class SongParcelable extends Song implements Parcelable {

  public static final Creator<SongParcelable> CREATOR = new Creator<SongParcelable>() {
    @Override
    public SongParcelable createFromParcel(Parcel p) {
      SongParcelable s = new SongParcelable();
      s.setSongName(p.readString());
      s.setArtistName(p.readString());
      s.setAlbumName(p.readString());
      s.setImageURL(p.readString());
      s.setSpotifyId(p.readString());
      return s;
    }

    @Override
    public SongParcelable[] newArray(int size) {
      return new SongParcelable[size];
    }
  };

  @Override
  public int describeContents() {
    return hashCode();
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(getSongName());
    parcel.writeString(getArtistName());
    parcel.writeString(getAlbumName());
    parcel.writeString(getImageURL());
    parcel.writeString(getSpotifyId());
  }

  public SongParcelable() {
  }

  public SongParcelable(Song s) {
    setSongName(s.getSongName());
    setArtistName(s.getArtistName());
    setAlbumName(s.getAlbumName());
    setImageURL(s.getImageURL());
    setSpotifyId(s.getSpotifyId());
  }

  public SongParcelable(Parcel p) {
    setSongName(p.readString());
    setArtistName(p.readString());
    setAlbumName(p.readString());
    setImageURL(p.readString());
    setSpotifyId(p.readString());
  }

  public static ArrayList<SongParcelable> convert(List<Song> songs) {
    ArrayList<SongParcelable> l = new ArrayList<SongParcelable>();
    for(Song s : songs) l.add(new SongParcelable(s));
    return l;
  }
}
