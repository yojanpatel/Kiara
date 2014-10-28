package uk.co.yojan.kiara.client.data.spotify;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Albums {

  @Expose
  private String href;
  @Expose
  private List<Album> items = new ArrayList<Album>();
  @Expose
  private Integer limit;
  @Expose
  private String next;
  @Expose
  private Integer offset;
  @Expose
  private Object previous;
  @Expose
  private Integer total;

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public List<Album> getAlbums() {
    return items;
  }

  public void setAlbums(List<Album> items) {
    this.items = items;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Object getPrevious() {
    return previous;
  }

  public void setPrevious(Object previous) {
    this.previous = previous;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

}