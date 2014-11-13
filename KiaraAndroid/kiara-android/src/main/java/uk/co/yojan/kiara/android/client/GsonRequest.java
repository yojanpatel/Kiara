/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android;

    import android.util.Log;
    import com.google.gson.Gson;
    import com.google.gson.JsonSyntaxException;

    import com.android.volley.AuthFailureError;
    import com.android.volley.NetworkResponse;
    import com.android.volley.ParseError;
    import com.android.volley.Request;
    import com.android.volley.Response;
    import com.android.volley.Response.ErrorListener;
    import com.android.volley.Response.Listener;
    import com.android.volley.toolbox.HttpHeaderParser;

    import java.io.UnsupportedEncodingException;
    import java.util.Map;

/**
 * Volley adapter for JSON requests with POST method that will be parsed into Java objects by Gson.
 */
public class GsonRequest<T> extends Request<T> {
  private Gson mGson = new Gson();
  private Class<T> clazz;
  private Map<String, String> headers;
  private Map<String, String> params;
  private Listener<T> listener;
  private byte[] body;

  /**
   * Make a GET request and return a parsed object from JSON.
   *
   * @param url URL of the request to make
   * @param clazz Relevant class object, for Gson's reflection
   */
  public GsonRequest(int method,
                     String url,
                     Class<T> clazz,
                     Listener<T> listener,
                     ErrorListener errorListener) {
    super(method, url, errorListener);
    this.clazz = clazz;
    this.listener = listener;
    mGson = new Gson();
  }

  /**
   * Make a POST request and return a parsed object from JSON.
   *
   * @param url URL of the request to make
   * @param clazz Relevant class object, for Gson's reflection
   */
  public GsonRequest(int method,
                     String url,
                     Class<T> clazz,
                     Map<String, String> params,
                     Listener<T> listener,
                     ErrorListener errorListener) {

    super(method, url, errorListener);
    this.clazz = clazz;
    this.params = params;
    this.listener = listener;
    this.headers = null;
    mGson = new Gson();
  }

  public GsonRequest(int method,
                     String url,
                     Class<T> clazz,
                     T obj,
                     Listener<T> listener,
                     ErrorListener errorListener) throws UnsupportedEncodingException {
    super(method, url, errorListener);
    this.clazz = clazz;
    this.listener = listener;
    mGson = new Gson();
    body = mGson.toJson(obj).getBytes("UTF-8");
  }

  @Override
  public byte[] getBody() throws AuthFailureError {
    return body != null ? body : super.getBody();
  }

  @Override
  public String getBodyContentType() {
    return body != null ? "application/json" : super.getBodyContentType();
  }

  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    return headers != null ? headers : super.getHeaders();
  }

  @Override
  protected Map<String, String> getParams() throws AuthFailureError {
    return params;
  }

  @Override
  protected void deliverResponse(T response) {
    listener.onResponse(response);
    Log.d("Volley", response.getClass().getName());
  }

  @Override
  protected Response<T> parseNetworkResponse(NetworkResponse response) {
    Log.d("Volley", "Parsing network response.");
    try {
      String json = new String(
          response.data, HttpHeaderParser.parseCharset(response.headers));
      Log.d("Volley", response.headers.toString());
      Log.d("Volley", json);
      return Response.success(
          mGson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
      Log.d("Volley", e.toString());
      return Response.error(new ParseError(e));
    } catch (JsonSyntaxException e) {
      Log.d("Volley", e.toString());
      return Response.error(new ParseError(e));
    }
  }
}