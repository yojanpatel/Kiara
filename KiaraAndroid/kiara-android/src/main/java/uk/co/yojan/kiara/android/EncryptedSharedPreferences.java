package uk.co.yojan.kiara.android;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;


/**
 * Thanks to emmby at http://stackoverflow.com/questions/785973/what-is-the-most-appropriate-way-to-store-user-settings-in-android-application/6393502#6393502
 * This class has the following additions over the original:
 *  additional logic for handling the case for when the preferences were not originally encrypted, but now are.
 *  The secret key is no longer hard coded, but defined at runtime based on the individual device.
 *  The benefit is that if one device is compromised, it now only affects that device.
 *
 * Simply replace your own SharedPreferences object in this one, and any data you read/write will be automatically encrypted and decrypted.
 *
 * Updated usage:
 *    ObscuredSharedPreferences prefs = ObscuredSharedPreferences.getPrefs(this, MY_APP_NAME, Context.MODE_PRIVATE);
 *    //to get data
 *    prefs.getString("foo", null);
 *    //to store data
 *    prefs.edit().putString("foo","bar").commit();
 */
public class EncryptedSharedPreferences implements SharedPreferences {
  protected static final String UTF8 = "UTF-8";
  private static char[] PBE_PASSWORD = null;

  protected static SharedPreferences delegate;
  protected Context context;
  private static EncryptedSharedPreferences prefs = null;

  /**
   * Constructor
   * @param context
   */
  public EncryptedSharedPreferences(Context context, String appName, int contextMode) {
    if(EncryptedSharedPreferences.delegate == null) {
      EncryptedSharedPreferences.delegate = context.getApplicationContext().getSharedPreferences(appName, contextMode);
    }
    this.context = context;
    PBE_PASSWORD = Constants.PBE_PASSWORD.toCharArray();
  }

  /**
   * Accessor to grab the preferences in a singleton.  This stores the reference in a singleton so it can be accessed repeatedly with
   * no performance penalty
   * @param c - the context used to access the preferences.
   * @return
   */
  public synchronized static EncryptedSharedPreferences getPrefs(Context c, String appName, int contextMode) {
    if (prefs == null) {
      // SharedPreferences live outside of the calling Activity's context.
      prefs = new EncryptedSharedPreferences(c.getApplicationContext(), appName, contextMode);
    }
    return prefs;
  }

  protected String encrypt( String value ) {
    try {
      final byte[] bytes = value!=null ? value.getBytes(UTF8) : new byte[0];
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PBE_PASSWORD));
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).getBytes(UTF8), 20));
      return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);
    } catch( Exception e ) {
      throw new RuntimeException(e);
    }

  }

  protected String decrypt(String value){
    try {
      final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PBE_PASSWORD));
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).getBytes(UTF8), 20));
      return new String(pbeCipher.doFinal(bytes),UTF8);
    } catch( Exception e) {
      Log.e(this.getClass().getName(), "Warning, could not decrypt the value.  It may be stored in plaintext.  "+e.getMessage());
      return value;
    }
  }

  public class Editor implements SharedPreferences.Editor {
    protected SharedPreferences.Editor delegateEditor;

    public Editor() {
      this.delegateEditor = EncryptedSharedPreferences.delegate.edit();
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
      delegateEditor.putString(key, encrypt(Boolean.toString(value)));
      return this;
    }

    @Override
    public Editor putFloat(String key, float value) {
      delegateEditor.putString(key, encrypt(Float.toString(value)));
      return this;
    }

    @Override
    public Editor putInt(String key, int value) {
      delegateEditor.putString(key, encrypt(Integer.toString(value)));
      return this;
    }

    @Override
    public Editor putLong(String key, long value) {
      delegateEditor.putString(key, encrypt(Long.toString(value)));
      return this;
    }

    @Override
    public Editor putString(String key, String value) {
      delegateEditor.putString(key, encrypt(value));
      return this;
    }

    @Override
    public void apply() {
      delegateEditor.commit();
    }

    @Override
    public Editor clear() {
      delegateEditor.clear();
      return this;
    }

    @Override
    public boolean commit() {
      return delegateEditor.commit();
    }

    @Override
    public Editor remove(String s) {
      delegateEditor.remove(s);
      return this;
    }

    @Override
    public android.content.SharedPreferences.Editor putStringSet(String key, Set<String> values) {
      throw new UnsupportedOperationException();
    }
  }

  public Editor edit() {
    return new Editor();
  }


  @Override
  public Map<String, ?> getAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getBoolean(String key, boolean defValue) {
    //if these weren't encrypted, then it won't be a string
    String v;
    try {
      v = delegate.getString(key, null);
    } catch (ClassCastException e) {
      return delegate.getBoolean(key, defValue);
    }

    return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
  }

  @Override
  public float getFloat(String key, float defValue) {
    String v;
    try {
      v = delegate.getString(key, null);
    } catch (ClassCastException e) {
      return delegate.getFloat(key, defValue);
    }
    try {
      return Float.parseFloat(decrypt(v));
    } catch (NumberFormatException e) {
      Log.e(this.getClass().getName(), "Warning, could not decrypt the value.  Possible incorrect key.  "+e.getMessage());
    }
    return defValue;
  }

  @Override
  public int getInt(String key, int defValue) {
    String v;
    try {
      v = delegate.getString(key, null);
    } catch (ClassCastException e) {
      return delegate.getInt(key, defValue);
    }
    try {
      return Integer.parseInt(decrypt(v));
    } catch (NumberFormatException e) {
      Log.e(this.getClass().getName(), "Warning, could not decrypt the value.  Possible incorrect key.  "+e.getMessage());
    }
    return defValue;
  }

  @Override
  public long getLong(String key, long defValue) {
    String v;
    try {
      v = delegate.getString(key, null);
    } catch (ClassCastException e) {
      return delegate.getLong(key, defValue);
    }
    try {
      return Long.parseLong(decrypt(v));
    } catch (NumberFormatException e) {
      Log.e(this.getClass().getName(), "Warning, could not decrypt the value.  Possible incorrect key.  " + e.getMessage());
    }
    return defValue;
  }

  @Override
  public String getString(String key, String defValue) {
    final String v = delegate.getString(key, null);
    return v != null ? decrypt(v) : defValue;
  }

  @Override
  public boolean contains(String s) {
    return delegate.contains(s);
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
  }

  @Override
  public Set<String> getStringSet(String key, Set<String> defValues) {
    throw new UnsupportedOperationException();
  }
}