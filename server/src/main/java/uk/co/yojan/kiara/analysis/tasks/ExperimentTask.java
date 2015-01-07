package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
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
//    experiment = ofy().load().key(Key.create(Experiment.class, spotifyPlaylistIds[0].split(":")[4])).now();
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
      assert api != null;
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
      assert playlist != null;
      List<PlaylistTrack> tracks = playlist.getTracks().getItems();
      List<String> trackIds = new ArrayList<>();
      for(PlaylistTrack t: tracks) {
        trackIds.add(t.getTrack().getId());
      }
      List<SongFeature> features = new ArrayList<>(OfyUtils.loadFeatures(trackIds));
      experiment.addPlaylist(features);
    }

//    String[] ids = {"7dS5EaCoMnN7DzlpT6aRn2",
//        "1VbhR6D6zUoSTBzvnRonXO",
//        "34gCuhDGsG4bRPIf9bb02f",
//        "7lUA4P03AhwAw40JHkdyGr",
//        "7pJgjBf82BrUQ3z7HdQvW1",
//        "6oDPg7fXW3Ug3KmbafrXzA",
//        "0nJW01T7XtvILxQgC5J7Wh",
//        "3b7ijsHsTPhGVgkb2DffFv",
//        "5ml7wqgv2ZZ5JJ6U8ZLon9",
//        "2hZuBpxfT40wqkammRWp1C",
//        "67wABU4SjBlnDHB0KiU3HL",
//        "3D4QFgYa3P9P0gjmv4eX6I",
//        "3U4isOIWM3VvDubwSI3y7a",
//        "4JehYebiI9JE8sR8MisGVb",
//        "3tIn65d1tiqGLPWatVgG22",
//        "7wZUrN8oemZfsEd1CGkbXE",
//        "4L2K7JKseFCBoHMZEAszW0",
//        "3QEdkjBmbgwTnfvuZL9U97",
//        "70yhaHLp9STtzI2Kzba6Tr",
//        "7DFNE7NO0raLIUbgzY2rzm",
//        "6l5AAEJhBR9Zku6qgI9Fo2",
//        "40xhyfAPDoMtv494MfPevP",
//        "3WSxYuV3ibaa8k7ujqPD5a",
//        "70WYUK1HKXHfwoDexKQHvd",
//        "1A2UmLDZzDmpdzUjEkCc3z",
//        "3JiockjOTd8m2VGcTGkmew",
//        "3LlAyCYU26dvFZBDUIMb7a",
//        "2hcPE11xSLCNHt5PYidj5U",
//        "7uCv1jz4ZpcKFIHupsoVq1",
//        "6XCPQygbj33s7OUxlbo6bX",
//        "1rqLUnSqJVsHSTLjrlMKSm",
//        "1zVhMuH7agsRe6XkljIY4U",
//        "4kflIGfjdZJW4ot2ioixTB",
//        "4EV2tN4g5BFRoLIVyo14O2",
//        "64ymP0l9Igq5ME7Qk7tqHa",
//        "78TKtlSLWK8pZAKKW3MyQL",
//        "0fYwfZcgijhIOyXn0RVPwq",
//        "4eHbdreAnSOrDDsFfc4Fpm",
//        "1dEy9Pl81QopSxNsPxXQxv",
//        "5AnCLGg35ziFOloEnXK4uu",
//        "7IHOIqZUUInxjVkko181PB",
//        "6Uy6K3KdmUdAfelUp0SeXn",
//        "4RL77hMWUq35NYnPLXBpih",
//        "7iEo3ipnJoBKW1ejz9vPYe",
//        "6BlHw1w21btaXLnYt7mLQp",
//        "0WeCJh7UNwcHGXWzwcprCn",
//        "1mKXFLRA179hdOWQBwUk9e",
//        "0VmdveDdlr2FMGwBljCfxA",
//        "0PKXBdefBWUYccMoTwuZh0",
//        "6ezDtFFRXi8RlRTlB2P4BJ",
//        "0dSchkfNB8SzYj8Bx7bcCW",
//        "01uqI4H13Gsd8Lyl1EYd8H",
//        "0XuhnKzXpHQYvCjycNVVdt",
//        "7mQruvaJWb7U18LX7p3m5r",
//        "6MCTFiVEptpgYjh4DdW3ck",
//        "4uTAiflWPDjhbjbMiZ4VeV",
//        "7sapKrjDij2fpDVj0GxP66",
//        "2nMeu6UenVvwUktBCpLMK9",
//        "2cyQ6WPm5elR3i4RtZiUmZ",
//        "1CAQnrKbdgFjrdx8PGoVqO",
//        "0apmvfS2EH4fC6P6O2MU0L",
//        "5KEOTXgjVwh7qwATk7QZPG",
//        "0YO6BduAqQHsjT10m8ObM3",
//        "2FiceoWDJ67rrmb5tGBpgE",
//        "4lY95OMGb9WxP6IYut64ir",
//        "1NrJYpdAi7uosDRPmSYrsG",
//        "4rHZZAmHpZrA3iH5zx8frV",
//        "3f3omU8n47Mqyab5nCaGyT",
//        "11EX5yhxr9Ihl3IN1asrfK",
//        "6AcLwmbR6ZsUm6zt7AgT1c",
//        "5cZ3QCfLRVBxMptA6HswII",
//        "4mqY9evKlKJAQGYwPSWHyb",
//        "2dBwB667LHQkLhdYlwLUZK",
//        "70AoHzwXbJBdz3DZ5fSDfY",
//        "71Jl2nL3f8h6a4uRjfhq89",
//        "0GDHTUVHcUghgelpLUSdqg",
//        "2PjDWdGvvbLQfRRTQIZPmv",
//        "6qOEjO2IUD7PjtpsXawq0d",
//        "2ih2U8ttFzCjnQ5njF3SrR",
//        "0Swvh86QCHSVk3yXUDeEM8",
//        "1wVcLKdJ4AFKPhKucNvEpy",
//        "13xxBnXOuiBxVxJI458B0i",
//        "4fwbGKNExPtPHbor1TBSY4",
//        "3AJwUDP919kvQ9QcozQPxg",
//        "0tp2cUnCAcsR6xL5kw9HKu",
//        "3CKCZ9pfwAfoMZlMncA1Nc",
//        "5cY957RFoP0c6BiWaq4dbF",
//        "5fVZC9GiM4e8vu99W0Xf6J",
//        "4NcywMnXKes4bGDG1Aqlf3",
//        "4pSPMXaCjbaV3VSzZQYC7H",
//        "3TCauNPqFiniaYHBvEVoHG",
//        "519tXYJVcrpEMqV2BMbh6E",
//        "3Q4WeJmzxuDpzMu9QjQqbM",
//        "51I4HLfbaGl5gEBA89i5JW",
//        "0VYCUWNFIYLIOQqu3bFzs0",
//        "52mdc3ScZWMjcR1qT9pJYB",
//        "7rdGrVIoqwPWOULauvglio",
//        "3nBmy2hAqIDmMOD0VZGB7I",
//        "25bNp65gYbwMevo5V1KoCA",
//        "5dzV75f9qVXVvdXLTqIG4L",
//        "1ku6iz80ht16xPwQNglXTv",
//        "0ZtLv7aVallYjnPJNZJWk7",
//        "0xuyDcVs9tGXEWy9iMPBXo",
//        "6JMtsVjR75qRma36b8nStS",
//        "0zY3dBKGFpwUaRsbuH6K6H",
//        "3mC4P2y840KDWKPjSwuaGx",
//        "3jzbzkSRXVHJC7yB1c8P2r",
//        "3lIiljMOADNWCvUvRVbADs",
//        "7xtDbIcvKaFDRnS02wM1wr",
//        "0n0p8VGjFfQSXnCKUMPCWU",
//        "3u7p51y9papT7QzyQ2jef6",
//        "7evqoCfGX7sRhEwvC5EKgu",
//        "6kCxcZRkvSYXYVuBGCkhzx",
//        "4zmT3KiW5UVfzGSIkYbs0y",
//        "6Yqc3pKYfVAGcC11r9rIrO",
//        "4fS4hGJcIydMZrWo68DFMf",
//        "1NjUGJUr4aR8xOGwWbdVNr",
//        "40f9IDTMDpFf3CnTcPhY5F",
//        "1QbbNXFnzqBH0NKULhkgsJ",
//        "4oxxBRHWfjfbOUtlRcYtCI",
//        "5Fj9rGLPKuSaRwsYSgfotv",
//        "2TFXdRFnNtBCIWjDtJ6oIX",
//        "0JExELrTTlXBvwYXJGIMtT",
//        "6J8SONJwAIizCJm0QhEi4Y",
//        "5MioM7cyUToVQN7zcIMVxi",
//        "0doeraoyarm1Rh8KNO5n7J",
//        "0P7wa8dTJYpy05CggXqkLB",
//        "0rOaRWuB9sml2q60FuB0I8",
//        "1Rezzt36ybaT2ZbDZpv83D",
//        "2myPv5Bj8NJPD5M624rqQy",
//        "5xrjJANQXCLXeMbNMugyaZ",
//        "60b9BBzPgohxv85Q4sSfG3",
//        "6dg4jncA9hbthBGFVo2djw",
//        "25vjNoOK2v7nmKnxEiWG1g",
//        "56BtIRP220yrOaDPwW6Wx7",
//        "01MXD4SH5HFdLIUSp1SL3H",
//        "1xfXPn56cFAEP9ReiIV8ha",
//        "2zsqZwmKKzdYdgRsBYbAg5",
//        "05Lrp3JkVJka4xmV3nbFdg",
//        "4D4RuVFzlNXSdf7IrBzE2o",
//        "0OsnLvp3vjq2yRA8gZqjrl",
//        "5TdAgcKS5HlxMcclStHODW",
//        "3BBiaTLrwRlSkozCTFtrsT",
//        "45QiVKJdwmEv3OM9ZLeWVA",
//        "1P5T6qvtEHtbiDOelBWGHD",
//        "3s2f6XU5sJ6aqEe7GgDBta",
//        "422AHQ9x72d6HAaYmnZhZ7",
//        "79a5QCOaeJyUygI5X936Ix",
//        "4xC3NPultaE0hCvUosYLSi",
//        "1Hn6tSoPeCKPqqOODgHP4y",
//        "2SPTGg9SC5MT1FwNX4IYfx",
//        "793jTClDbqmxFme8QUl0JE",
//        "2OteNxrcPHNKhtohrV2VXN",
//        "5BzZtDfhntg4FDj2UUYrX1",
//        "0oks4FnzhNp5QPTZtoet7c",
//        "2Foc5Q5nqNiosCNqttzHof",
//        "1r9mGafUiSgumJoRqyLrSt",
//        "0dh2F3Mi1b1EGafGmvUC9M",
//        "4lteJuSjb9Jt9W1W7PIU2U",
//        "6uomknm6K7oOeRDfwBzuV7",
//        "48csJKRBHm6gnp0d0bQCqz",
//        "49yNskzLtR1VNTWhSF90mu",
//        "4IJUO9cdEvsHVDk52UZVHr",
//        "4SMciQoW2tOVieBAg6U2Qj",
//        "1wp9SHLdY9bMy7pCa2fd7C",
//        "7LxmIcbK52R9TPHghXVAce",
//        "5Ei38Uh4aqwKVrjIM8ptcb",
//        "1GaYqv2NMMlVbG3ewJQ4A6",
//        "5brviRvO3ZIDvdF0dED9wC",
//        "7EipTiVLdacMPjyHs7YF4H",
//        "3jnZjSqydlIYAZhY8eHLEr",
//        "0zbuvyQQFhJCKFWScdY1C5",
//        "6oOcmP1u2ObaNE51WbFOIZ",
//        "49yNskzLtR1VNTWhSF90mu",
//        "3eOOtOQPhfPdmr65r3LshN",
//        "6nS5Vfs6Kv75ZFUa2MKvaz",
//        "5zLABygBfwMj7tbEq9XHbJ",
//        "3TsF817rKVwEWgEH65zkEd",
//        "5gfQQoGnAwmO74Ln8lVhWn",
//        "1P0wLge0z6CiDMztVH0q9i",
//        "1ku6iz80ht16xPwQNglXTv",
//        "7LuojryrN6VkYWbax5dWTv",
//        "0Zw4llKeB35SIKASy1aLtV",
//        "0kuCcDu31Aqy4sgYL1ERQM",
//        "5R4hGpNiUxoDpKx1RLHvzL",
//        "6zLEnNn5ccx7WV304xwXnG",
//        "0faHbZQeLjeJ0x1Ebxn7O5",
//        "5mf8jY9FsGCAuWwJMmKIzF",
//        "4XxbBFfKlXY2E5PN3dHAfz",
//        "16MBgVIBhdZjORLDrcLU2c",
//        "2C0AjvO0ysSUpJOzr5nkZJ",
//        "14mYQm3y6GMmfylkL1DsFg",
//        "0xiuSFBOEHWaJ6rSuUbJWc",
//        "5ffYRhCQSt9VF5yaJkh44r",
//        "2sNAjuCXxyj8jHt93t9IJ9",
//        "3tyUh8UpK0PVu00AjbP1UG",
//        "58s4iqgXFzXhBndBkg2AaD",
//        "5HwTPPZb9hNuTpLMhk8jth",
//        "3OWsm3HE35S9x3fgY8tbgh",
//        "1UmB9ikmRilZyCzRk0VH4u",
//        "6mz1fBdKATx6qP4oP1I65G",
//        "6TlRNJaezOdzdECnQeRuMM",
//        "6l4TNbfPtNAOOlGIfceSaO",
//        "17IuQqSZsngvxzK8mN7OeC",
//        "7MQywXGHEev7JmwwIzMcao",
//        "1pJ8UmVeImPBvyDGH1DCLt",
//        "4eMGevfobDTaWfr3TLbkFm",
//        "3TkNsftUBh8dwwLUHpmYNC",
//        "2bZfoRmADhlF5gMCSZ4pDS",
//        "3pbpimHVpBabe4YnKEOaqN",
//        "2FbD2sqUmAdjqqCBp0mn2g",
//        "4l2ecMeHvDg2Y9OOkqfucN",
//        "3WFwsXIUqV6tmHSjTaXUHR",
//        "06ZDLodo1UDVavc3MXREZ3",
//        "0EwaWLbMW0Tq84YVFNxNsd",
//        "7dfl9X38RhIvB3oSmmneq3",
//        "10b8G4LwhvNOPgwF68fW5p",
//        "3seflYvRN3gmh50Iih5yMd",
//        "0PBPO8rmYjmehH4j4ymOxl",
//        "2HAVFycrhtbmLxyyxpm6JI",
//        "7alcrjZSE1TGO6qtb0pQRU",
//        "5TBrB7l0nf80AcGTBC0y9X",
//        "5UwXd4AktGqBXmRJe5RvBP",
//        "6jUyYHDikmw9WltPojSR37",
//        "3Ypnlj9KYSif4Elk7EeHgt",
//        "5sYzy3L4JCFrAZbTLVTFFr",
//        "6ctr1K7KzKErxbfGAynL8A",
//        "7y5A3bZR4RK7GReqaSNhBh",
//        "3j94Y4l3yHrYxFppX78EJI",
//        "3TS25nchaYIPb2Y2fw6cbb",
//        "3vbIA1AKIo9RhZb26XZNtz",
//        "6hxn98poTu1O4YZfafvC18",
//        "6I3LsZncKrJG9mwv12FCEg",
//        "6xrvubhznLOcEO3AM6XBN2",
//        "5iKYZAcAchghry6h8Iu5IL",
//        "2fyTojfPYs67KBWN4WYRX7",
//        "31bf9SEOppLU6lQ85d8om6",
//        "2OeSwSbwlmn9u8SsutfxPO",
//        "2WfhlEjoUII31H6imnQdvF",
//        "0xikWgPgYN9BEes0ieZ8Co",
//        "1DkGOPjC2OXN7xcT31fjXN",
//        "2Bc4XnUlkgPvwvfwsCbgcD",
//        "47TqCCnEliDp8NRDyIQoQq"};
//    List<String> trackIds = Arrays.asList(ids);
//    List<SongFeature> features = new ArrayList<>(OfyUtils.loadFeatures(trackIds));
//    Logger.getLogger("").warning(trackIds.size() + " " + features.size() + " features loaded");
//    experiment.addPlaylist(features);

    // <mean, variance, median, min, max, range, skewness, kurtosis>

    // check each timbre base vector on its own
//    ArrayList<double[]> timbreWeights = timbreWeights();
//    for(double[] ws : timbreWeights) {
//      try {
//        experiment.runNewExperiment(timbreWeights(ws, 0.0, 0.0, 0.0, 0.0));
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }
//
//    // check each statistical moment on its own
//    ArrayList<double[]> statWeights = statWeights();
//    for(double[] ws : statWeights) {
//      try {
//        experiment.runNewExperiment(featureWeights(ws, 0.0, 0.0, 0.0, 0.0));
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }

    double[] ws = {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
    try {
      // normalize the other features
//      experiment.runNewExperiment(featureWeights(ws, 1000.0, 1000.0, 1000.0, 1000.0));
      // normalize half
//      experiment.runNewExperiment(featureWeights(ws, 50.0, 50.0, 50.0, 50.0));
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
