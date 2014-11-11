package uk.co.yojan.kiara.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class FullImageView extends ImageView {

  public FullImageView(Context context) {
    super(context);
  }


  public FullImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FullImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(width, width);
  }
}
