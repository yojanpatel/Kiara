package uk.co.yojan.kiara.android.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.adapters.BrowseAdapter;
import uk.co.yojan.kiara.android.views.SlidingTabLayout;

public class BrowseTabActivity extends KiaraActivity {

  private long playlistId;

  private BrowseAdapter mAdapter;
  @InjectView(R.id.pager) ViewPager pager;
  @InjectView(R.id.sliding_tabs) SlidingTabLayout mSlidingTabLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.fragment_add);
    ButterKnife.inject(this);

    getIntent().getLongExtra(Constants.ARG_PLAYLIST_ID, -1);

    mAdapter = new BrowseAdapter(getFragmentManager(), playlistId);
    pager.setAdapter(mAdapter);

    mSlidingTabLayout.setDistributeEvenly(true);
    mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.indigo500));
    mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.pinkA200));

    mSlidingTabLayout.setViewPager(pager);
  }


}
