package uk.co.yojan.kiara.android.utils;

import android.graphics.*;
import com.squareup.picasso.Transformation;

/*
 * A transformation used by Picasso to crop an image to a circle.
 */
public class CircularCropTransformation implements Transformation {

  // TODO: constructor which take a boolean square to skip some computation if the source is a square already.

  /**
   * Transform the source bitmap into a new bitmap. If you create a new bitmap instance, you must
   * call {@link android.graphics.Bitmap#recycle()} on {@code source}. You may return the original
   * if no transformation is required.
   *
   * @param source
   */
  @Override
  public Bitmap transform(Bitmap source) {
    int height = source.getHeight();
    int width = source.getWidth();

    Bitmap squared = source;
    int size = Math.min(height, width);
    // If source isn't already a square, convert it.
    if(height != width) {
      int x = (width - size) / 2;
      int y = (height - size) / 2;
      squared = Bitmap.createBitmap(source, x, y, size, size);
      source.recycle();
    }

    Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();

    // replace edge colour if the shader draws outside of the original bounds.
    BitmapShader shader = new BitmapShader(squared,
        BitmapShader.TileMode.CLAMP,
        BitmapShader.TileMode.CLAMP);
    paint.setShader(shader);
    paint.setAntiAlias(true);

    float r = size / 2F;
    canvas.drawCircle(r, r, r, paint);

    squared.recycle();
    return bitmap;
  }

  /**
   * Returns a unique key for the transformation, used for caching purposes. If the transformation
   * has parameters (e.g. size, scale factor, etc) then these should be part of the key.
   */
  @Override
  public String key() {
    return "CIRCLE_CROP";
  }
}
