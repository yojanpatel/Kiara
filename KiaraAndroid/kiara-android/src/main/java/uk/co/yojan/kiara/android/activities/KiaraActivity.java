package uk.co.yojan.kiara.android.activities;

import android.app.Activity;
import android.view.Window;
import android.widget.Toast;
import com.squareup.otto.Bus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyApiInterface;

/**
 * Base Activity class which performs functionality common to all activities
 * in the app.
 */
public class KiaraActivity extends Activity {

  private Bus mBus;

  private boolean registeredToBus;

  @Override
  public void onResume() {
    super.onStart();
    if(!registeredToBus)
      getBus().register(this);
    registeredToBus = true;
    getKiaraApplication().eventBuffer().stopAndProcess();
  }

  @Override
  public void onPause() {
    super.onStop();
    getKiaraApplication().eventBuffer().startSaving();
    if(registeredToBus)
      getBus().unregister(this);
    registeredToBus = false;
    Crouton.cancelAllCroutons();
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

  public SpotifyApiInterface spotifyWebApi() {
    return getKiaraApplication().spotifyWebApi();
  }

  public KiaraApiInterface kiaraApi() {
    return getKiaraApplication().kiaraApi();
  }

  public KiaraApplication getKiaraApplication() {
    return (KiaraApplication)getApplication();
  }

  public EncryptedSharedPreferences sharedPreferences() {
    return getKiaraApplication().sharedPreferences();
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

  public void removeTitleBar() {
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }
}
