package uk.co.yojan.kiara.server.resources;

import com.wrapper.spotify.exceptions.WebApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yojan on 11/29/14.
 */
public class Test {

  public static void main(String[] args) throws IOException, WebApiException {
    List<String> s = new ArrayList<String>();
    s.add("spotify:track:dsdsafa");
    s.add("\"hello\"");
    s.add("\"spotify:track:yojan\"");
    s.add("patel");


    for(int i = 0; i < s.size(); i++) {
      s.set(i, s.get(i).replace("\"", "").replace("spotify:track:", ""));
    }

    for(String str : s) {
      System.out.println(str);
    }
  }

}
