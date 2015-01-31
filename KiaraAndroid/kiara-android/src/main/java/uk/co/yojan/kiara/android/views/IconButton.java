package uk.co.yojan.kiara.android.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import uk.co.yojan.kiara.android.R;

public class IconButton extends ImageView {

  private static final int PRESSED_COLOR_LIGHTUP = 255 / 25;
  private static final int PRESSED_RING_ALPHA = 30;
  private static final int DEFAULT_PRESSED_RING_WIDTH_DIP = 4;
  private static final int ANIMATION_TIME_ID = android.R.integer.config_shortAnimTime;

  private int centerY;
  private int centerX;
  private int outerRadius;

  private Paint focusPaint;

  private float animationProgress;

  private int pressedRingWidth;
  private int defaultColor = Color.BLACK;
  private int pressedColor;

  private boolean outerRing = false;

  private int startRadius = 16;
  private int finishRadius;
  private int color = Color.argb(64,255,255,255); // 25% opacity white

  private ObjectAnimator pressedAnimator;
  private ObjectAnimator alphaAnimator;

  public IconButton(Context context) {
    super(context);
    init(context, null);
  }

  public IconButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public IconButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  @Override
  public void setPressed(boolean pressed) {
    super.setPressed(pressed);

    if (pressed) {
      showPressedRing();
    } else {
      hidePressedRing();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if(animationProgress > 0) {
      if(outerRing) canvas.drawCircle(centerX, centerY, startRadius + pressedRingWidth, focusPaint);
      canvas.drawCircle(centerX, centerY, startRadius + animationProgress, focusPaint);
    }
    super.onDraw(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    centerX = w / 2;
    centerY = h / 2;
    outerRadius = Math.min(w, h) / 2;
  }

  public float getAnimationProgress() {
    return animationProgress;
  }

  public void setAnimationProgress(float animationProgress) {
    this.animationProgress = animationProgress;
    this.invalidate();
  }

  public void setColor(int color) {
    this.defaultColor = color;
    focusPaint.setColor(defaultColor);
    focusPaint.setAlpha(PRESSED_RING_ALPHA);

    this.invalidate();
  }

  private void hidePressedRing() {
    outerRing = false;
    pressedAnimator.setFloatValues(pressedRingWidth, 0f);
    pressedAnimator.start();
  }

  private void showPressedRing() {
    outerRing = true;
    pressedAnimator.setFloatValues(animationProgress, pressedRingWidth);
    pressedAnimator.start();
  }

  private void init(Context context, AttributeSet attrs) {
    this.setFocusable(true);
    this.setScaleType(ScaleType.CENTER_INSIDE);
    setClickable(true);

    focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    focusPaint.setStyle(Paint.Style.FILL);

    pressedRingWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PRESSED_RING_WIDTH_DIP, getResources()
        .getDisplayMetrics());

    int color = Color.BLACK;
    if (attrs != null) {
      final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconButton);
      color = a.getColor(R.styleable.IconButton_ib_color, color);
      pressedRingWidth = (int) a.getDimension(R.styleable.IconButton_ib_pressedRingWidth, pressedRingWidth);
      startRadius = (int) a.getDimension(R.styleable.IconButton_ib_startRingWidth, 0);
      a.recycle();
    }

    setColor(color);
    final int pressedAnimationTime = getResources().getInteger(ANIMATION_TIME_ID);
    pressedAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 0f);
    pressedAnimator.setDuration(pressedAnimationTime);
  }
}