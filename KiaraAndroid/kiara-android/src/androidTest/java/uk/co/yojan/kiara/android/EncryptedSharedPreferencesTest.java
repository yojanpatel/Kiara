package uk.co.yojan.kiara.android;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by yojan on 10/13/14.
 */
public class EncryptedSharedPreferencesTest extends ActivityInstrumentationTestCase2 {

  Context context;
  EncryptedSharedPreferences esp;


  public EncryptedSharedPreferencesTest(Class activityClass) {
    super(activityClass);
  }

  @Before
  public void setUp() {
    context = getInstrumentation().getTargetContext();
    esp = new EncryptedSharedPreferences(context);
  }

  @Test
  public void testSetText() {

  }

  @Test
  public void testGetText() {

  }

}
