package uk.co.yojan.kiara.client.data.spotify;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class SpotifyUser {

  @Expose
  private String country;
  @SerializedName("display_name")
  @Expose
  private String displayName;
  @Expose
  private String email;
  @Expose
  private String href;
  @Expose
  private String id;
  @Expose
  private List<Image> images = new ArrayList<Image>();
  @Expose
  private String product;
  @Expose
  private String type;
  @Expose
  private String uri;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getPrimaryImageURL() {
    List<Image> imgs = getImages();
    if(imgs != null && imgs.size() > 0) {
      return imgs.get(0).getUrl();
    }
    return null;
  }

}