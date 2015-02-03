package uk.co.yojan.kiara.android.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.adapters.BrowseAdapter;
import uk.co.yojan.kiara.android.events.BatchAddSongs;
import uk.co.yojan.kiara.android.events.GetSongsForPlaylist;
import uk.co.yojan.kiara.android.fragments.SelectDialog;
import uk.co.yojan.kiara.android.views.SlidingTabLayout;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;
import java.util.HashSet;

public class BrowseTabActivity extends KiaraActivity implements ViewPager.OnPageChangeListener, OnSongSelectionListener {

  private long playlistId;
  private String playlistName;

  public static final int SEARCH = 0;
  public static final int IMPORT = 1;

  private BrowseAdapter mAdapter;

  @InjectView(R.id.pager) ViewPager pager;
  @InjectView(R.id.sliding_tabs) SlidingTabLayout mSlidingTabLayout;
  @InjectView(R.id.shadow) View shadow;

  // Toolbar views
  private EditText searchEdit;
  private ImageButton reset;
  private ImageView search;

  // Bottombar views
  private Toolbar bottomBar;
  private boolean bottomBarVisible;
  private TextView selectedText;
  private TextView addAllButton;

  int width, height;

  private InputMethodManager imm;

  // Animators
  ViewPropertyAnimator tbAnimatorUp, tabAnimatorUp, shAnimatorUp;
  ViewPropertyAnimator tbAnimatorDown, tabAnimatorDown, shAnimatorDown;

  private HashSet<Track> selectedSongs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_browse);
    ButterKnife.inject(this);

    // Get screen size for resizing
    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;
    height = size.y;

    playlistId = getIntent().getLongExtra(Constants.ARG_PLAYLIST_ID, -1);
    playlistName = getIntent().getStringExtra(Constants.ARG_PLAYLIST_NAME);

    pager.setOnPageChangeListener(this);
    mSlidingTabLayout.setOnPageChangeListener(this);
    mAdapter = new BrowseAdapter(getFragmentManager(), playlistId);
    pager.setAdapter(mAdapter);

    mSlidingTabLayout.setDistributeEvenly(true);
    mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.indigo500));
    mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.pinkA200));

    View toolbarLayout = getToolbar().findViewById(R.id.toolbarLayout);
    searchEdit = (EditText) toolbarLayout.findViewById(R.id.searchEditText);
    reset = (ImageButton) toolbarLayout.findViewById(R.id.resetQueryButton);
    search = (ImageView) toolbarLayout.findViewById(R.id.search_icon);


    bottomBar = (Toolbar) findViewById(R.id.bottombar);
    View bottombarLayout = bottomBar.findViewById(R.id.bottomBarLayout);
    selectedText = (TextView) bottombarLayout.findViewById(R.id.bottomBarText);
    addAllButton = (TextView) bottombarLayout.findViewById(R.id.addAllButton);

    mSlidingTabLayout.setViewPager(pager);
    initializeDialogButton();
    imm = (InputMethodManager)getSystemService(
        Context.INPUT_METHOD_SERVICE);
  }

  @Override
  public void onPageScrolled(int i, float v, int i1) {
    Log.d("BrowseTabActivity", "onPageScrolled");
  }

  @Override
  public void onPageSelected(int position) {

    if(position == IMPORT) {
      searchEdit.setVisibility(View.INVISIBLE);
      reset.setVisibility(View.INVISIBLE);
      search.setVisibility(View.INVISIBLE);

      // Hide the soft keyboard when Import tab chosen
      imm.hideSoftInputFromInputMethod(searchEdit.getWindowToken(), 0);

      if(bottomBarVisible) {
        animateBottomBarDown();
      }

      animateToolbarUp();
    }
    else if(position == SEARCH) {
      searchEdit.setVisibility(View.VISIBLE);
      search.setVisibility(View.VISIBLE);

      if(bottomBarVisible) {
        animateBottomBarUp();
      }

      animateToolbarDown();
      // show the soft keyboard connected to the search
      imm.showSoftInputFromInputMethod(searchEdit.getWindowToken(), 0);
    }
  }

  @Override
  public void onPageScrollStateChanged(int i) {
    Log.d("BrowseTabActivity", "onPageScrollStateChanged");

  }

  @Override
  public void onSongSelectionChanged(HashSet<Track> songs) {

    if(songs.isEmpty() && bottomBarVisible) {
      bottomBarVisible = false;
      animateBottomBarDown();
    } else if(!songs.isEmpty()) {
      String text = songs.size() + " songs selected";
      if(bottomBarVisible) {
        selectedText.setText(text);
      } else {
        animateBottomBarUp(text);
        bottomBarVisible = true;
      }
    }
    // keep a reference to the selected songs
    selectedSongs = songs;
  }


  public void animateToolbarUp() {
    tbAnimatorUp = toolbar.animate().translationY(-toolbar.getHeight())
        .setInterpolator(new AccelerateInterpolator())
        .setDuration(300)
        .setListener(new Animator.AnimatorListener() {
          public void onAnimationStart(Animator animation) {
          }

          public void onAnimationEnd(Animator animation) {
            toolbar.setVisibility(View.INVISIBLE);
          }

          public void onAnimationCancel(Animator animation) {
          }

          public void onAnimationRepeat(Animator animation) {
          }
        });

    tabAnimatorUp = mSlidingTabLayout.animate().translationY(-toolbar.getHeight())
        .setInterpolator(new AccelerateInterpolator())
        .setDuration(300);

    shAnimatorUp = shadow.animate().translationY(-toolbar.getHeight())
        .setInterpolator(new AccelerateInterpolator())
        .setDuration(300);

    tbAnimatorUp.start();
    tabAnimatorUp.start();
    shAnimatorUp.start();
  }


  public void animateToolbarDown() {
    tbAnimatorDown = toolbar.animate().translationY(0)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(300)
        .setListener(new Animator.AnimatorListener() {
          public void onAnimationStart(Animator animation) {
            toolbar.setVisibility(View.VISIBLE);
          }

          public void onAnimationEnd(Animator animation) {
          }

          public void onAnimationCancel(Animator animation) {
          }

          public void onAnimationRepeat(Animator animation) {
          }
        });

    tabAnimatorDown = mSlidingTabLayout.animate().translationY(0)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(300);

    shAnimatorDown = shadow.animate().translationY(0)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(300);

    tbAnimatorDown.start();
    tabAnimatorDown.start();
    shAnimatorDown.start();
  }

  private void animateBottomBarUp(String text) {
    selectedText.setText(text);
    animateBottomBarUp();
  }

  private void animateBottomBarUp() {
    Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
    bottomBar.startAnimation(bottomUp);
    bottomBar.setVisibility(View.VISIBLE);
  }

  private void animateBottomBarDown() {
    final Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
    bottomDown.setAnimationListener(new Animation.AnimationListener() {
      public void onAnimationStart(Animation animation) {
      }

      public void onAnimationEnd(Animation animation) {
        bottomBar.setVisibility(View.GONE);
      }

      public void onAnimationRepeat(Animation animation) {
      }
    });
    bottomBar.startAnimation(bottomDown);
  }

  public HashSet<Track> selectedSongs() {
    return selectedSongs;
  }

  private void initializeDialogButton() {
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SelectDialog dialog = SelectDialog.newInstance();
        dialog.show(getSupportFragmentManager(), "SelectDialog");
      }
    };
    bottomBar.setOnClickListener(listener);
    addAllButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ArrayList<Track> tracks = new ArrayList<Track>();
        for(Track t : selectedSongs()) {
          tracks.add(t);
        }
        getBus().post(new BatchAddSongs(tracks, playlistId()));
        toast(tracks.size() + " songs added.");

        Intent i = new Intent(BrowseTabActivity.this, PlaylistSongListActivity.class);
        i.putExtra(Constants.ARG_PLAYLIST_ID, playlistId());
        i.putExtra(Constants.ARG_PLAYLIST_NAME, playlistName());
        getBus().post(new GetSongsForPlaylist(playlistId()));
        startActivity(i);
        finish();
      }
    });

  }

  public long playlistId() {
    return playlistId;
  }

  public String playlistName() {
    return playlistName;
  }
}
