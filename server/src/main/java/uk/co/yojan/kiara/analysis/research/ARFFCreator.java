package uk.co.yojan.kiara.analysis.research;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.PlaylistRequest;
import com.wrapper.spotify.models.Playlist;
import com.wrapper.spotify.models.PlaylistTrack;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.KMeans;
import uk.co.yojan.kiara.analysis.features.scaling.MinMaxScaler;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.SpotifyApi.spotifyApi;


public class ARFFCreator {
  public static String fileName = "edm-jazz-rnb.arff";
  public static String[] spotifyPlaylistIds = {"spotify:user:spotify:playlist:2ujjMpFriZ2nayLmrD1Jgl",
      "spotify:user:spotify:playlist:7ECmf74Ey57LAYulSxhL9w",
      "spotify:user:spotify:playlist:04MJzJlzOoy5bTytJwDsVL"};


  public static HashMap<String, Integer> nomMap= new HashMap<>();
  static int clusterId = 0;

  public static List<SongFeature> loadPlaylist() {
    // download from spotify
    Api api = null;
    try {
      api = spotifyApi();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (WebApiException e) {
      Logger.getLogger("s").warning(e.getMessage());
      e.printStackTrace();
    }
    List<String> trackIds = new ArrayList<>();

    for(String playlistURI : spotifyPlaylistIds) {
      String[] uri = playlistURI.split(":");
      String user = uri[2];
      String playlistId = uri[4];
      PlaylistRequest request = api.getPlaylist(user, playlistId).build();
      Playlist playlist = null;
      try {
        playlist = request.get();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (WebApiException e) {
        Logger.getLogger("s").warning(e.getMessage());
        e.printStackTrace();
      }
      List<PlaylistTrack> tracks = playlist.getTracks().getItems();
      for(PlaylistTrack t: tracks) {
        trackIds.add(t.getTrack().getId());
        nomMap.put(t.getTrack().getId(), clusterId);
      }
      clusterId++;
    }
    return new ArrayList<>(OfyUtils.loadFeatures(trackIds));
  }

  private static String getInstanceLine(SongFeature f) {
    StringBuilder sb = new StringBuilder();

    // pitches
    for(int i = 0; i < 12; i++)
      for(Double d : f.getPitchMoment(i))
      sb.append(d).append(",");
    // timbres
    for(int i = 0; i < 12; i++)
      for(Double d : f.getTimbreMoment(i))
        sb.append(d).append(",");
    // tempos
    sb.append(f.getBarLengthMean()).append(",");
    sb.append(f.getBarLengthVar()).append(",");
    sb.append(f.getTatumLengthMean()).append(",");
    sb.append(f.getTatumLengthVar()).append(",");
    sb.append(f.getMaxSectionTempo()).append(",");
    sb.append(f.getMinSectionTempo()).append(",");
    sb.append(f.getNormalisedTempo()).append(",");
    // other
    sb.append(f.getLoudness()).append(",");
    sb.append(f.getValence()).append(",");
    sb.append(f.getEnergy());

    return sb.toString();
  }

  private static String getInstanceLine2(SongFeature f) throws IllegalAccessException {
    StringBuilder sb = new StringBuilder();
    double[] vals = f.getFeatureValues();
    for(double d : vals) {
      sb.append(d).append(",");
    }
    sb.append(nomMap.get(f.getId()));
    // remove last comma
    return sb.toString();
  }

  private static String getInstanceLine2(String id, Instance instance) {
    double[] vals = instance.toDoubleArray();
    StringBuilder sb = new StringBuilder();
    for(double d : vals) {
      sb.append(d).append(",");
    }
    sb.append(nomMap.get(id));
    // remove last comma
    return sb.toString();
  }

  public static String constructInstances() throws FileNotFoundException, UnsupportedEncodingException, IllegalAccessException {
    List<String> attrnames = SongFeature.getFeatureNames();
    List<SongFeature> features = loadPlaylist();
    Instances instances = KMeans.constructDataSet(features);
    Instances normalized = new MinMaxScaler().scale(instances);

//    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
    StringBuilder sb = new StringBuilder();

//    writer.println("@relation playlist");
    sb.append("@relation playlist\n");
    for(String attName : attrnames) {
//      writer.println("@attribute " + attName + " numeric");
      sb.append("@attribute " + attName + " numeric\n");
    }
    sb.append("@attribute playlist {");
    for(int i = 0; i < spotifyPlaylistIds.length; i++) {
      sb.append(i + ", ");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("}\n");
//    writer.println();
    sb.append("\n");

//    writer.println("@data");
    sb.append("@data\n");
    for(int i = 0; i < normalized.numInstances(); i++) {
      Instance instance = normalized.instance(i);
      SongFeature f = features.get(i);
//      writer.println(getInstanceLine(f));
      sb.append(getInstanceLine2(f.getId(), instance) + "\n");
    }
//    writer.close();

    return sb.toString();
  }


  public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
    List<String> attrnames = SongFeature.getFeatureNames();


    PrintWriter writer = new PrintWriter(fileName, "UTF-8");

    writer.println("@relation playlist");
    for(String attName : attrnames) {
      writer.println("@attribute " + attName + " numeric");
    }
    writer.println();

    List<SongFeature> features = loadPlaylist();
    writer.println("@data");
    for(SongFeature f : features) {
      writer.println(getInstanceLine(f));
    }
    writer.close();
  }

}
