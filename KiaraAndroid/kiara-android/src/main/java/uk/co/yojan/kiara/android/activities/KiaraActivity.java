package uk.co.yojan.kiara.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import retrofit.RetrofitError;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyApiInterface;

/**
 * Base Activity class which performs functionality common to all activities
 * in the app.
 */
public class KiaraActivity extends Activity {

  private boolean bound;
  private MusicService musicService;

  private Bus mBus;

  ProgressBar progressBar;

  private boolean registeredToBus;

  @Override
  public void onStart() {
    super.onStart();

    Intent bind = new Intent(this, MusicService.class);
    // bindService(bind, mConnection, Context.BIND_AUTO_CREATE);

    if(!registeredToBus)
      getBus().register(this);
    registeredToBus = true;
    getKiaraApplication().eventBuffer().stopAndProcess();
  }

  @Override
  public void onStop() {
    super.onStop();

    // Unbind from the service
    /*
    if (bound) {
      unbindService(mConnection);
      bound = false;
    }*/

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

  public MusicService getMusicService() {
    return musicService;
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

  public void addIndeterminateProgressBar() {
    // create new ProgressBar and style it
    progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    progressBar.setIndeterminate(true);
    progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
    final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
    decorView.addView(progressBar);

    ViewTreeObserver observer = progressBar.getViewTreeObserver();
    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        View contentView = decorView.findViewById(android.R.id.content);
        progressBar.setY(contentView.getY() - 10);

        ViewTreeObserver observer = progressBar.getViewTreeObserver();
        observer.removeGlobalOnLayoutListener(this);
      }
    });
  }

  public void setProgressBarVisibility(int visible) {
    if(progressBar != null) {
      progressBar.setVisibility(visible);
    }
  }


  @Subscribe
  public void onError(VolleyError error) {
    toast("Oops. Something went wrong.");
  }

  @Subscribe
  public void onError(RetrofitError error) {
    toast("Oops. Something went wrong.");
  }
}
