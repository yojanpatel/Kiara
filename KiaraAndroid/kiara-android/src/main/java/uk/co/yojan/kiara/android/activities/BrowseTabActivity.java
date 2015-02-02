package uk.co.yojan.kiara.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.adapters.BrowseAdapter;
import uk.co.yojan.kiara.android.views.SlidingTabLayout;

public class BrowseTabActivity extends KiaraActivity implements ViewPager.OnPageChangeListener {

  private long playlistId;

  public static final int SEARCH = 0;
  public static final int IMPORT = 1;

  private BrowseAdapter mAdapter;
  @InjectView(R.id.pager) ViewPager pager;
  @InjectView(R.id.sliding_tabs) SlidingTabLayout mSlidingTabLayout;

  private InputMethodManager imm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_browse);
    ButterKnife.inject(this);

    playlistId = getIntent().getLongExtra(Constants.ARG_PLAYLIST_ID, -1);
    pager.setOnPageChangeListener(this);
    mSlidingTabLayout.setOnPageChangeListener(this);
    mAdapter = new BrowseAdapter(getFragmentManager(), playlistId);
    pager.setAdapter(mAdapter);

    mSlidingTabLayout.setDistributeEvenly(true);
    mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.indigo500));
    mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.pinkA200));

    mSlidingTabLayout.setViewPager(pager);

    imm = (InputMethodManager)getSystemService(
        Context.INPUT_METHOD_SERVICE);
  }

  @Override
  public void onPageScrolled(int i, float v, int i1) {
    Log.d("BrowseTabActivity", "onPageScrolled");
  }

  @Override
  public void onPageSelected(int position) {
    Log.d("BrowseTabActivity", "Page selected " + position);
    View toolbarLayout = getToolbar().findViewById(R.id.toolbarLayout);
    if(position == IMPORT) {
      toolbarLayout.findViewById(R.id.searchEditText).setVisibility(View.INVISIBLE);
      toolbarLayout.findViewById(R.id.resetQueryButton).setVisibility(View.INVISIBLE);
      toolbarLayout.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
      toolbarLayout.findViewById(R.id.titleBrowsePlaylists).setVisibility(View.VISIBLE);


    }
    else if(position == SEARCH) {
      toolbarLayout.findViewById(R.id.searchEditText).setVisibility(View.VISIBLE);
      toolbarLayout.findViewById(R.id.search_icon).setVisibility(View.VISIBLE);
      toolbarLayout.findViewById(R.id.titleBrowsePlaylists).setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onPageScrollStateChanged(int i) {
    Log.d("BrowseTabActivity", "onPageScrollStateChanged");

  }
}
