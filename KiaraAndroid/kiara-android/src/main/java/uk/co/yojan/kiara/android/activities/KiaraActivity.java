package uk.co.yojan.kiara.android.activities;

import android.app.Activity;
import com.squareup.otto.Bus;
import uk.co.yojan.kiara.android.KiaraApplication;

import java.util.Date;

/**
 * Base Activity class which performs functionality common to all activities
 * in the app.
 */
public class KiaraActivity extends Activity {

  private Bus mBus;

  @Override
  public void onResume() {
    super.onResume();
    getBus().register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getBus().unregister(this);
  }

  public Bus getBus() {
    if(mBus == null) {
      mBus = getKiaraApplication().getBus();
    }
    return mBus;
  }

  public void setBus(Bus bus) {
    this.mBus = bus;
  }

  public KiaraApplication getKiaraApplication() {
    return (KiaraApplication)getApplication();
  }

  public long getTimestamp() {
    return System.currentTimeMillis();
  }
}
