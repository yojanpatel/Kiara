package uk.co.yojan.kiara.server.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by yojan on 12/22/14.
 */
@Entity
public class UserToken {

  @Id
  String name;
  String access_token;
  String refresh_token;
  long expiry_date;

  public UserToken(String name, String access_token, String refresh_token, long expiry_date) {
    this.name = name;
    this.access_token = access_token;
    this.refresh_token = refresh_token;
    this.expiry_date = expiry_date;
  }

  public String getName() {
    return name;
  }

  public String getAccess_token() {
    return access_token;
  }

  public String getRefresh_token() {
    return refresh_token;
  }

  public long getExpiry_date() {
    return expiry_date;
  }
}
