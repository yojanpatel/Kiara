package uk.co.yojan.kiara.android.utils;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by yojan on 12/1/14.
 */
public class BusProvider {
  private static final Bus bus = new Bus(ThreadEnforcer.ANY) {
    /**
     * Posts an event to all registered handlers.  This method will return successfully after the event has been posted to
     * all handlers, and regardless of any exceptions thrown by handlers.
     * <p/>
     * <p>If no handlers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event event to post.
     * @throws NullPointerException if the event is null.
     */
    @Override
    public void post(Object event) {
      super.post(event);
      Log.d("BUS", event.getClass().getSimpleName());
    }
  };

  public static Bus getInstance() {
    return bus;
  }

  private BusProvider(){}
}
