package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.PlaylistRequest;
import com.wrapper.spotify.models.Playlist;
import com.wrapper.spotify.models.PlaylistTrack;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.research.Experiment;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;
import static uk.co.yojan.kiara.server.SpotifyApi.spotifyApi;


public class ExperimentTask implements DeferredTask {

  Experiment experiment;
  String spotifyIds;
  int k;

  public ExperimentTask(String spotifyPlaylistIds, int k) throws IOException, WebApiException {
    this.spotifyIds = spotifyPlaylistIds;
    this.k = k;
  }

  @Override
  public void run() {
    String[] spotifyPlaylistIds = spotifyIds.split(";");
    experiment = ofy().load().key(Key.create(Experiment.class, spotifyPlaylistIds[0].split(":")[4])).now();
    if(experiment == null)
        experiment = new Experiment(spotifyPlaylistIds[0].split(":")[4]);

    experiment.setK(k);
    // load playlists
    Api api = null;
    try {
      api = spotifyApi();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (WebApiException e) {
      Logger.getLogger("s").warning(e.getMessage());
      e.printStackTrace();
    }
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
      List<String> trackIds = new ArrayList<>();
      for(PlaylistTrack t: tracks) {
        trackIds.add(t.getTrack().getId());
      }
      List<SongFeature> features = new ArrayList<>(OfyUtils.loadFeatures(trackIds));
      experiment.addPlaylist(features);
    }

    // <mean, variance, median, min, max, range, skewness, kurtosis>

    // check each timbre base vector on its own
    ArrayList<double[]> timbreWeights = timbreWeights();
    for(double[] ws : timbreWeights) {
      try {
        experiment.runNewExperiment(timbreWeights(ws, 0.0, 0.0, 0.0, 0.0));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // check each statistical moment on its own
    ArrayList<double[]> statWeights = statWeights();
    for(double[] ws : statWeights) {
      try {
        experiment.runNewExperiment(featureWeights(ws, 0.0, 0.0, 0.0, 0.0));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    double[] ws = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
    try {
      // normalize the other features
      experiment.runNewExperiment(featureWeights(ws, 1000.0, 1000.0, 1000.0, 1000.0));
      // normalize half
      experiment.runNewExperiment(featureWeights(ws, 50.0, 50.0, 50.0, 50.0));
      // control
      experiment.runNewExperiment(featureWeights(ws, 1.0, 1.0, 1.0, 1.0));
    } catch (Exception e) {
      e.printStackTrace();
    }

    ofy().save().entity(experiment).now();
  }

  public static ArrayList<Double> featureWeights(double[] statWeights, double tempoWeight, double loudnessWeight, double energyWeight, double valenceWeight) {
    ArrayList<Double> weights = new ArrayList<>();
    for(int i = 0; i < 12; i++) {
      for(int j = 0; j < statWeights.length; j++) {
        weights.add(statWeights[j]);
      }
    }
    weights.add(tempoWeight);
    weights.add(loudnessWeight);
    weights.add(energyWeight);
    weights.add(valenceWeight);
    return weights;
  }

  public static ArrayList<Double> timbreWeights(double[] timbreWeights, double tempoWeight, double loudnessWeight, double energyWeight, double valenceWeight) {
    ArrayList<Double> weights = new ArrayList<>();
    for(int i = 0; i < timbreWeights.length; i++) {
      for(int j = 0; j < 8; j++) {
        weights.add(timbreWeights[i]);
      }
    }
    weights.add(tempoWeight);
    weights.add(loudnessWeight);
    weights.add(energyWeight);
    weights.add(valenceWeight);
    return weights;
  }

  public static ArrayList<double[]> statWeights() {
    ArrayList<double[]> weights = new ArrayList<>();
    for(int i = 0; i < 8; i++) {
      double[] ws = new double[8];
      ws[i] = 1.0;
      weights.add(ws);
    }
    return weights;
  }

  public static ArrayList<double[]> timbreWeights() {
    ArrayList<double[]> weights = new ArrayList<>();
    for(int i = 0; i < 12; i++) {
      double[] ws = new double[12];
      ws[i] = 1.0;
      weights.add(ws);
    }
    return weights;
  }
}
