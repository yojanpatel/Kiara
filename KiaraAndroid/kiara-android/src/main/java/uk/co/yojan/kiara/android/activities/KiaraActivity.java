package uk.co.yojan.kiara.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.background.MusicService;
import uk.co.yojan.kiara.android.events.*;
import uk.co.yojan.kiara.android.utils.ConnectionDetector;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyApiInterface;
import uk.co.yojan.kiara.client.data.spotify.SpotifyUser;

/**
 * Base Activity class which performs functionality common to all activities
 * in the app.
 */
public class KiaraActivity extends ActionBarActivity {
  private static final String LOG = KiaraActivity.class.getName();

  private ConnectionDetector detector;

  private boolean bound;
  private MusicService musicService;

  private Bus mBus;

  Toolbar toolbar;
  FloatingActionButton fab;
  ProgressBar progressBar;

  private boolean registeredToBus;

  // callback for when all stages of authentication are complete
  public AuthenticationCallback authCallback;

  /**
   * Every activity has responsibility to check Spotify access token is still valid,
   * and refresh if not.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    detector = new ConnectionDetector(getApplicationContext());
    if(!detector.isConnectingToInternet()) {
      showAlertDialog();
    }

    if (loggedIn()) {
      if (accessExpired()) {
        // If refreshToken exists, we can use that to get another access token.
        Log.d(LOG, "Access Token has expired.");
        String refreshToken = sharedPreferences().getString(Constants.REFRESH_TOKEN, null);
        if (refreshToken != null) {
          Log.d(LOG, "Requesting Access Token refresh using the refresh token.");
          getBus().post(new RefreshAccessTokenRequest(getUserId(), refreshToken));
        }
      } else {
        Log.d(LOG, "logged in and access token is still valid.");
        authCallback();
      }
    } else {
      showLogin();
    }
  }

  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    if(toolbar != null) {
      setSupportActionBar(toolbar);
      Log.d(LOG, (getSupportActionBar() == null) + " gsab");
    }
    fab = (FloatingActionButton) findViewById(R.id.fab);
  }

  @Override
  public void onStart() {
    Log.d(LOG, "onStart()");
    super.onStart();

    Intent bind = new Intent(this, MusicService.class);
    // bindService(bind, mConnection, Context.BIND_AUTO_CREATE);

    if(!registeredToBus) {
      getBus().register(this);
      getBus().register(authEventHandler);
    }


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
    if(registeredToBus) {
      getBus().unregister(this);
      getBus().unregister(authEventHandler);
    }
    registeredToBus = false;

    Crouton.cancelAllCroutons();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Uri uri = intent.getData();
    if(uri != null) {
      AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
      String code = response.getCode();
      getBus().post(new AuthCodeGrantRequest(code));
    }
  }

  private void showLogin() {
    // go to main activity and prompt to log in.
    if(!getClass().equals(MainActivity.class)) {
      Intent main = new Intent(this, MainActivity.class);
      main.setAction(Constants.AUTHENTICATE);
      main.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
      startActivity(main);
    }
  }

  private final Object authEventHandler = new Object() {

    @Subscribe
    public void onForceLogin(ForceRelogin event) {
      showLogin();
    }

    @Subscribe
    public void onAuthCodeGrantComplete(AuthCodeGrantResponse event) {
      Log.d(LOG, "AuthCodeGrant flow complete.");
      sharedPreferences().edit()
          .putString(Constants.ACCESS_TOKEN, event.getAccessToken())
          .putLong(Constants.ACCESS_DEADLINE, getTimestamp() + (1000 * (event.getExpiresIn() - 60)))
          .putString(Constants.REFRESH_TOKEN, event.getRefreshToken())
          .commit();
      getBus().post(new CurrentUserRequest());
    }

    @Subscribe
    public void onRefreshAccessComplete(RefreshAccessTokenResponse event) {
      Log.d(LOG, "RefreshAccessComplete.");
      sharedPreferences().edit()
          .putString(Constants.ACCESS_TOKEN, event.getAccessToken())
          .putLong(Constants.ACCESS_DEADLINE, getTimestamp() + (1000 * (event.getExpiresIn() - 60)))
          .commit();
      authCallback();
    }

    // Get basic user information and update the shared preferences.
    @Subscribe
    public void onCurrentUser(SpotifyUser user) {
      EncryptedSharedPreferences.Editor edit = sharedPreferences().edit();
      edit.putString(Constants.USER_ID, user.getId())
        .putString(Constants.USER_IMG_URL, user.getPrimaryImageURL())
        .putString(Constants.USER_TYPE, user.getType())
        .putString(Constants.USER_COUNTRY, user.getCountry());
      edit.commit();
      getKiaraApplication().initKiaraService(user.getId());
      authCallback();
    }
  };


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

  private void authCallback() {
    getKiaraApplication().initKiaraService(getUserId());
    String accessToken = sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
//    if(accessToken != null)
//      getKiaraApplication().updateSpotifyService(accessToken);
    if(authCallback != null) {
      authCallback.onAccessTokenValidated();
    }
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

  public void addIndeterminateProgressBar() {
    // create new ProgressBar and style it
    progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    progressBar.setIndeterminate(true);
    progressBar.getIndeterminateDrawable().setColorFilter(
        getResources().getColor(R.color.pinkA200), PorterDuff.Mode.SRC_IN);
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

  public Toolbar getToolbar() {
    return toolbar;
  }

  public FloatingActionButton getFab() {
    return fab;
  }

  public boolean accessExpired() {
    return getKiaraApplication().accessExpired();
  }

  protected boolean loggedIn() {
    return getUserId() != null;
//    return false;
  }

  public String getUserId() {
    return getKiaraApplication().getUserId();
  }
  public String getCountry() {
    return sharedPreferences().getString(Constants.USER_COUNTRY, "");
  }


  public void initialiseAuthCallbacks(AuthenticationCallback authCallback) {
    Log.d(LOG, "init callbacks " + (authCallback == null));
    this.authCallback = authCallback;
  }

  private void showAlertDialog() {
    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle("No Internet Connection");
    alertDialog.setMessage("Please connect to the internet to use Kiara");
    alertDialog.setIcon(android.R.drawable.stat_sys_warning);

    // Setting OK Button
    alertDialog.setButton("Connect", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
      }
    });

    // Showing Alert Message
    alertDialog.show();
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if(item.getItemId() == R.id.logout_menu) {
      sharedPreferences().edit().clear().commit();
      Intent main = new Intent(this, MainActivity.class);
      startActivity(main);
      finish();
    }

    return super.onOptionsItemSelected(item);
  }
}
