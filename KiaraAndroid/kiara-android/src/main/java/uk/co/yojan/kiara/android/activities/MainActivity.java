package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;

import java.util.Random;


public class MainActivity extends KiaraActivity
  implements ConnectionStateCallback, AuthenticationCallback {

  private String LOG = MainActivity.class.getName();

  private Window window;

  @InjectView(R.id.title) TextView kiaraTitle;
  @InjectView(R.id.loginButton) ImageButton login;
  @InjectView(R.id.progressBar) ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    initialiseAuthCallbacks(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.inject(this);
    Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/basictitlefont.ttf");
    kiaraTitle.setTypeface(tf);

    window = getWindow();

    // Translucent status bar
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    pickBackground();
    if(!loggedIn()) {
      progressBar.setVisibility(View.GONE);
      login.setVisibility(View.VISIBLE);
      login.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Log.d(LOG, "Authenticating via Authenticate Code Grant Flow with Spotify.");
          SpotifyAuthentication.openAuthWindow(Constants.CLIENT_ID, "code", Constants.REDIRECT_URI,
              new String[]{"user-read-private", "streaming"}, null, MainActivity.this);
        }
      });
    }
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

  private void pickBackground() {
    int choice = new Random().nextInt(2);
    if(choice == 0) {
      findViewById(R.id.relative).setBackground(getResources().getDrawable(R.drawable.background_pink));
    } else if(choice == 1) {
      findViewById(R.id.relative).setBackground(getResources().getDrawable(R.drawable.background_blue));
    } else if(choice == 2) {
      findViewById(R.id.relative).setBackground(getResources().getDrawable(R.drawable.background_purple));

    }
  }
}
