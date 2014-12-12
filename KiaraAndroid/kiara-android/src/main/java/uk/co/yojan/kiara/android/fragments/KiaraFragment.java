package uk.co.yojan.kiara.android.fragments;

import android.app.Fragment;
import com.squareup.otto.Bus;
import uk.co.yojan.kiara.android.KiaraApplication;

public class KiaraFragment extends Fragment {

  private Bus mBus;
  private boolean registeredToBus;



  @Override
  public void onStart() {
    super.onStart();
    if(!registeredToBus)
      getBus().register(this);
    registeredToBus = true;
    getKiaraApplication().eventBuffer().stopAndProcess();
  }

  @Override
  public void onStop() {
    super.onStop();
    getKiaraApplication().eventBuffer().startSaving();
    if(registeredToBus)
      getBus().unregister(this);
    registeredToBus = false;
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
