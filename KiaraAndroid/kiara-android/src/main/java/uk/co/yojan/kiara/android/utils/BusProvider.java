package uk.co.yojan.kiara.android.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by yojan on 12/1/14.
 */
public class BusProvider {
  private static final Bus bus = new Bus(ThreadEnforcer.ANY);

  public static Bus getInstance() {
    return bus;
  }

  private BusProvider(){}
}
