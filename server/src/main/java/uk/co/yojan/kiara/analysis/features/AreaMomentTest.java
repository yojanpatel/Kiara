package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;

import java.util.ArrayList;

/**
 * Created by yojan on 1/21/15.
 */
public class AreaMomentTest {


  public static void main(String[] args) {
//    String id = args[0];
//    String artist = args[1];
//    String title = args[2];

    String[] ids = {"01e5hRr3UOuhCNJOOTIB8d", "2wBCrtJS3E3TimRZ5MElTI", "0CrKpt3spBLCDMuziyFv4k","0FZj3IxSkUOTgEzT7dEG4P", "0NB68WBmKfJNC3fhhqfZGv"};


    for(String id : ids) {
      SongAnalysis songAnalysis = EchoNestApi.getSongAnalysis(id);
      // As a fallback, search EchoNest with the artist and title name of the song.
      // This often works for obscure or new tracks.
      if (songAnalysis == null) {
        System.out.println("Failed to search with the Spotify Id, trying to search using artist name and title.");
        //      songAnalysis = EchoNestApi.getSongAnalysis(artist, title);
      }

      songAnalysis.setId(id);
      SongData songData = songAnalysis.getSongData();

      ArrayList<Double> a = new AreaMomentTimbre(songData.getSegments()).get();
      System.out.println(a.toString());
    }
  }
}
