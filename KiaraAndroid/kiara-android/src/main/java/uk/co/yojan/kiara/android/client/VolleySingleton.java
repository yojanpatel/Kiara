package uk.co.yojan.kiara.android.client;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * RequestQueue singleton.
 *
 * Maintains a global instance for the Request Queue that manages the REST calls.
 *
 */
public class VolleySingleton {

  private static VolleySingleton instance;
  private RequestQueue requestQueue;

  private VolleySingleton(Context context) {
    requestQueue = Volley.newRequestQueue(context);

  }

  public static VolleySingleton getInstance(Context context) {
    if(instance == null) {
      instance = new VolleySingleton(context);
    }
    return instance;
  }


  public RequestQueue getRequestQueue() {
    return requestQueue;
  }

  public <T> void addToRequestQueue(Request<T> req) {
    try {
      Log.d("Volley", "Requesting " + req.getUrl());
      if(requestQueue.getCache().get(req.getUrl()) != null)
        Log.d("Volley", "Etag: " + requestQueue.getCache().get(req.getUrl()).etag);
      if(req.getHeaders() != null) Log.d("Volley", req.getHeaders().toString());
      if(req.getBody() != null) Log.d("Volley", new String(req.getBody()));
    } catch (AuthFailureError authFailureError) {
      Log.e("Volley", "Couldn't display request body.");
    }
    req.setTag("Kiara");
    getRequestQueue().add(req);
  }

  public <T> T getFromCache(String url, Class<T> clazz) {

    Log.d("Volley", "Trying to fetch " + url + " " + clazz.getCanonicalName() + " from cache.");
    Cache.Entry cached = requestQueue.getCache().get(url);
    if(cached == null) {
      return null;
    }
    Log.d("Volley", "Cached entry exists. Trying to parse to return it.");
    try {
      return new Gson().fromJson(new String(cached.data, "UTF-8"), clazz);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
