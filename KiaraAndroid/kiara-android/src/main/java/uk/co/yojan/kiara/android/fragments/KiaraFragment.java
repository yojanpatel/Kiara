package uk.co.yojan.kiara.android.fragments;

import android.app.Fragment;
import android.util.Log;
import com.squareup.otto.Bus;
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.activities.KiaraActivity;

public class KiaraFragment extends Fragment {

  private Bus mBus;
  private boolean registeredToBus;

  private KiaraActivity parent;


  @Override
  public void onStart() {
    super.onStart();
    if(!registeredToBus) {
      Log.d("KiaraFragment", "Registering to bus.");
      getBus().register(this);
    }
    registeredToBus = true;
    getKiaraApplication().eventBuffer().stopAndProcess();
  }

  @Override
  public void onStop() {
    super.onStop();
    getKiaraApplication().eventBuffer().startSaving();
    if(registeredToBus) {
      Log.d("KiaraFragment", "Registering to bus.");
      getBus().unregister(this);
    }
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

  public KiaraActivity getKiaraActivity() {
    if(parent == null) parent = (KiaraActivity) getActivity();
    return parent;
  }
}
