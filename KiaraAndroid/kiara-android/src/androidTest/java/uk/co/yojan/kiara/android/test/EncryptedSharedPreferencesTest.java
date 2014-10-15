package uk.co.yojan.kiara.android.test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import uk.co.yojan.kiara.android.EncryptedSharedPreferences;


public class EncryptedSharedPreferencesTest extends ActivityInstrumentationTestCase2 {

  public EncryptedSharedPreferencesTest() {
    super(EncryptedSharedPreferences.class);
  }

  Context context;
  EncryptedSharedPreferences esp;


  public EncryptedSharedPreferencesTest(Class activityClass) {
    super(activityClass);
  }

  @Override
  public void setUp() {
    context = getInstrumentation().getTargetContext();
    esp = EncryptedSharedPreferences.getPrefs(context, "TEST-PREFS", Context.MODE_PRIVATE);
  }

  public void testPutString() {
    esp.edit().putString("Test", "TestValue#1").commit();
    assertNotNull(esp.getString("Test", null));
    assertEquals(esp.getString("Test", null), "TestValue#1");
  }

  public void testPutLong() {
    esp.edit().putLong("Test", 3232443421L).commit();
    assertEquals(esp.getLong("Test", 0), 3232443421L);

    esp.edit().putLong("Test2", 0L).commit();
    assertEquals(esp.getLong("Test2", 1L), 0L);
  }
}
