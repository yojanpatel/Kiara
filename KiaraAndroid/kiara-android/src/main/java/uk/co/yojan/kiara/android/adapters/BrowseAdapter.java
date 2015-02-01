package uk.co.yojan.kiara.android.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import uk.co.yojan.kiara.android.activities.BrowseTabActivity;
import uk.co.yojan.kiara.android.fragments.SearchFragment;
import uk.co.yojan.kiara.android.fragments.SpotifyPlaylistFragment;


public class BrowseAdapter extends FragmentPagerAdapter {

  private long playlistId;

  public BrowseAdapter(FragmentManager fm) {
    super(fm);
  }

  public BrowseAdapter(FragmentManager fm, long playlistId) {
    super(fm);
    this.playlistId = playlistId;
  }

  @Override
  public Fragment getItem(int position) {

    Fragment fragment = null;

    // ImportFragment
    if(position == BrowseTabActivity.IMPORT) {
      fragment = SpotifyPlaylistFragment.newInstance(playlistId);
    } // SearchFragment
    else if(position == BrowseTabActivity.SEARCH) {
      fragment = SearchFragment.newInstance(playlistId);
    }
    return fragment;
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Override
  public CharSequence getPageTitle(int position) {

    if(position == BrowseTabActivity.IMPORT) {
      return "Import";
    } else if(position == BrowseTabActivity.SEARCH) {
      return "Search";
    }
    return "";
  }
}
