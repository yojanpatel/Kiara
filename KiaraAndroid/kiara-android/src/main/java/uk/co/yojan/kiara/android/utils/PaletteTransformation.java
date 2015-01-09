package uk.co.yojan.kiara.android.utils;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.util.Log;
import com.squareup.picasso.Transformation;

import java.util.Map;
import java.util.WeakHashMap;

public final class PaletteTransformation implements Transformation {

  private static final PaletteTransformation INSTANCE = new PaletteTransformation();
  private static final Map<Bitmap, Palette> CACHE = new WeakHashMap();

  public static PaletteTransformation instance() {
    return INSTANCE;
  }

  public static Palette getPalette(Bitmap bitmap) {
    Log.d("transform", CACHE.size() + " cache size");
    for(Bitmap b : CACHE.keySet()) Log.d("transform", b.hashCode() + " cached hashcode");
    Log.d("transform", bitmap.hashCode() + " hashcode");
    return CACHE.get(bitmap);
  }

  private PaletteTransformation() {}

  @Override public Bitmap transform(Bitmap source) {
    Palette palette = Palette.generate(source);
    Log.e("transform", "Transform: " + (palette == null) + " " + source.hashCode());
    CACHE.put(source, palette);
    return source;
  }

  /**
   * Returns a unique key for the transformation, used for caching purposes. If the transformation
   * has parameters (e.g. size, scale factor, etc) then these should be part of the key.
   */
  @Override
  public String key() {
    return "";
  }
}