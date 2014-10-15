package uk.co.yojan.kiara.android.activities;

import android.app.Activity;
import android.widget.Toast;
import com.squareup.otto.Bus;
import uk.co.yojan.kiara.android.KiaraApplication;

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

  public void toast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
  }

  public void toast(String text, boolean lengthShort) {
    Toast.makeText(this, text, lengthShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
  }
}
