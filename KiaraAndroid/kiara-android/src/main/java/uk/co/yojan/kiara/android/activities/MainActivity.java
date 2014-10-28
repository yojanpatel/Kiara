package uk.co.yojan.kiara.android.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.events.*;
import uk.co.yojan.kiara.client.data.spotify.SpotifyUser;


public class MainActivity extends KiaraActivity
  implements ConnectionStateCallback {

  private String LOG = getClass().getName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
      getBus().post(new RefreshAccessTokenResponse(accessToken, -1));
      finishLoading();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

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
  }

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

  public boolean accessExpired() {
    String accessToken = sharedPreferences().getString(Constants.ACCESS_TOKEN, null);
    if(accessToken == null) {
      return false;
    }
    return (sharedPreferences().getLong(Constants.ACCESS_DEADLINE, 0L) < getTimestamp());
  }

  // Event Bus listeners.
  @Subscribe
  public void onAuthCodeGrantComplete(AuthCodeGrantResponse event) {
    Log.d(LOG, "AuthCodeGrant flow complete.");
    sharedPreferences().edit()
        .putString(Constants.ACCESS_TOKEN, event.getAccessToken())
        .putLong(Constants.ACCESS_DEADLINE, getTimestamp() + (1000 * (event.getExpiresIn() - 60)))
        .putString(Constants.REFRESH_TOKEN, event.getRefreshToken())
        .commit();
    finishLoading();
  }

  @Subscribe
  public void onRefreshAccessComplete(RefreshAccessTokenResponse event) {
    sharedPreferences().edit()
        .putString(Constants.ACCESS_TOKEN, event.getAccessToken())
        .putLong(Constants.ACCESS_DEADLINE, getTimestamp() + (1000 * (event.getExpiresIn() - 60)))
        .commit();
    finishLoading();
  }

  public void finishLoading() {
    // UI change, intent triggered for next screen.
    toast("Authenticated.");

    // Get the current user's details since they may have been updated.
    getBus().post(new CurrentUserRequest());
  }

  // Get basic user information and update the shared preferences.
  @Subscribe
  public void onCurrentUser(SpotifyUser user) {
    sharedPreferences().edit()
        .putString(Constants.USER_ID, user.getId())
        .putString(Constants.USER_IMG_URL, user.getPrimaryImageURL())
        .putString(Constants.USER_TYPE, user.getType()).commit();
    ((KiaraApplication)getApplication()).initKiaraService(user.getId());
    Toast.makeText(getApplicationContext(), sharedPreferences().getString(Constants.USER_ID, null), Toast.LENGTH_SHORT).show();

    goToPlaylistViewActivity();
  }

  private void goToPlaylistViewActivity() {
    startActivity(new Intent(this, PlaylistViewActivity.class));
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
