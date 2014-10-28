package uk.co.yojan.kiara.client.data.spotify;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Artists {

  @Expose
  private String href;
  @Expose
  private List<Artist> items = new ArrayList<Artist>();
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

  public List<Artist> getArtists() {
    return items;
  }

  public void setArtists(List<Artist> items) {
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