package uk.co.yojan.kiara.android.fragments;

import android.app.Fragment;
import com.squareup.otto.Bus;
import uk.co.yojan.kiara.android.KiaraApplication;

public class KiaraFragment extends Fragment {

  private Bus mBus;

  @Override
  public void onResume() {
    super.onResume();
    getBus().register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getBus().unregister(this);
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

  public KiaraApplication getKiaraApplication() {
    return (KiaraApplication)getActivity().getApplication();
  }
}
