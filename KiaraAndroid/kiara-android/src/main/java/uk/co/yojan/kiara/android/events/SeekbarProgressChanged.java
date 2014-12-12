package uk.co.yojan.kiara.android.events;

import android.widget.SeekBar;

/**
 * Created by yojan on 12/3/14.
 */
public class SeekbarProgressChanged {
  public SeekBar seekBar;
  public int progress;
  public boolean fromUser;

  public SeekbarProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    this.seekBar = seekBar;
    this.progress = progress;
    this.fromUser = fromUser;
  }
}
