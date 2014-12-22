package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import uk.co.yojan.kiara.android.R;


public class MainActivity extends KiaraActivity
  implements ConnectionStateCallback, AuthenticationCallback {

  private String LOG = MainActivity.class.getName();

  @InjectView(R.id.title) TextView kiaraTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    initialiseAuthCallbacks(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    ButterKnife.inject(this);
    Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/basictitlefont.ttf");
    kiaraTitle.setTypeface(tf);

    /*
    if(accessExpired()) {

      // If refreshToken exists, we can use that to get another access token.
      Log.d(LOG, "Access Token has expired.");
      String refreshToken = sharedPreferences().getString(Constants.REFRESH_TOKEN, null);
      if (refreshToken != null) {
        Log.d(LOG, "Requesting Access Token refresh using the refresh token.");
        getBus().post(new RefreshAccessTokenRequest(refreshToken));
      } else {
        Log.d(LOG, "Authenticating via Authenticate Code Grant Flow with Spotify.");
        SpotifyAuthentication.openAuthWindow(Constants.CLIENT_ID, "code", Constants.REDIRECT_URI,
            new String[]{"user-read-private", "streaming"}, null, this);
      }
    } else {
      String accessToken = sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
//      getBus().post(new RefreshAccessTokenResponse(accessToken, -1));
      finishLoading();
    } */
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  /*
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Uri uri = intent.getData();
    if(uri != null) {
      AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
      String code = response.getCode();
      Log.d(LOG, "Code: " + code);
      getBus().post(new AuthCodeGrantRequest(code));
    }
  } */

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onAccessTokenValidated() {
    Log.d(LOG, "onAccessTokenValidated");
    Intent i = new Intent(this, PlaylistViewActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
    startActivity(i);
    finish();
  }

  // ConnectionState Callbacks.

  @Override
  public void onLoggedIn() {
    Log.d(LOG, "User logged in.");
  }

  @Override
  public void onLoggedOut() {
    Log.d(LOG, "User logged out.");
  }

  @Override
  public void onLoginFailed(Throwable throwable) {
    Log.d(LOG, "Login failed.");
    throwable.printStackTrace();
  }

  @Override
  public void onTemporaryError() {
    Log.d(LOG, "Temporary error occurred.");
  }

  @Override
  public void onNewCredentials(String s) {
    Log.d(LOG, "User credentials blob received.");
  }

  @Override
  public void onConnectionMessage(String s) {
    Log.d(LOG, "Received connection message: " + s);
  }
}
