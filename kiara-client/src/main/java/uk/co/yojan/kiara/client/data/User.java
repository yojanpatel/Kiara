package uk.co.yojan.kiara.client.data;

import com.google.gson.annotations.Expose;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class User {

  @Expose
  private String id;
  @Expose
  private String firstName;
  @Expose
  private String lastName;
  @Expose
  private String facebookId;
  @Expose
  private String email;
  @Expose
  private String imageURL;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFacebookId() {
    return facebookId;
  }

  public void setFacebookId(String facebookId) {
    this.facebookId = facebookId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

}
