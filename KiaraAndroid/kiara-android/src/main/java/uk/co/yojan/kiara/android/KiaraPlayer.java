package uk.co.yojan.kiara.android;

import android.util.Log;
import com.spotify.sdk.android.playback.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Subclassing Player to allow usage of raw PCM data if needed later on.
 */
public class KiaraPlayer extends Player {

  Player player;

  public KiaraPlayer(InitializationObserver observer, NativePlayer nativePlayer) {
    super(observer, nativePlayer);
  }

  public static KiaraPlayer create(final Config config, final InitializationObserver observer/*, NativePlayer nativePlayer*/) {

    NativeSdkPlayer sdkPlayer = new NativeSdkPlayer() {
      @Override
      public void registerAudioDeliveredCallback(AudioDeliveredCallback callback) {
        super.registerAudioDeliveredCallback(callback);
        Log.d("KiaraPlayer", "1. registerAudioDelvieredCallback");
      }
    };

    final KiaraPlayer kp = new KiaraPlayer(observer, sdkPlayer);
    sdkPlayer.registerAudioDeliveredCallback(kp);

    kp.setPlayer(Player.create(new NativeSdkPlayer() {
      @Override
      public void registerAudioDeliveredCallback(AudioDeliveredCallback callback) {
        super.registerAudioDeliveredCallback(kp);
      }
    }, config, observer));
    return kp;
  }

  @Override
  public int onAudioDelivered(short[] frames, int numFrames, int sampleRate, int channels) {
//    Log.d("KiaraPlayer", frames.length + " frames delivered at " + sampleRate + " sample rate.");
    return player.onAudioDelivered(frames, numFrames, sampleRate, channels);
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @Override
  public void execute(Runnable runnable) {
    player.execute(runnable);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
    return player.invokeAny(callables, l, timeUnit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws InterruptedException, ExecutionException {
    return player.invokeAny(callables);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit) throws InterruptedException {
    return player.invokeAll(callables, l, timeUnit);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
    return player.invokeAll(callables);
  }

  @Override
  public Future<?> submit(Runnable runnable) {
    return player.submit(runnable);
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T result) {
    return player.submit(runnable, result);
  }

  @Override
  public <T> Future<T> submit(Callable<T> callable) {
    return player.submit(callable);
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    return player.awaitTermination(l, timeUnit);
  }

  @Override
  public boolean isInitialized() {
    return player.isInitialized();
  }

  @Override
  public boolean isTerminated() {
    return player.isTerminated();
  }

  @Override
  public boolean isShutdown() {
    return player.isShutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return player.shutdownNow();
  }

  @Override
  public void shutdown() {
    player.shutdown();
  }

  @Override
  public void addConnectionStateCallback(ConnectionStateCallback callback) {
    player.addConnectionStateCallback(callback);
  }

  @Override
  public void removeConnectionStateCallback(ConnectionStateCallback callback) {
    player.removeConnectionStateCallback(callback);
  }

  @Override
  public void onLoggedIn() {
    player.onLoggedIn();
  }

  @Override
  public void onLoggedOut() {
    player.onLoggedOut();
  }

  @Override
  public void onLoginFailed(Throwable error) {
    player.onLoginFailed(error);
  }

  @Override
  public void onTemporaryError() {
    player.onTemporaryError();
  }

  @Override
  public void onNewCredentials(String credentialsBlob) {
    player.onNewCredentials(credentialsBlob);
  }

  @Override
  public void onConnectionMessage(String message) {
    player.onConnectionMessage(message);
  }

  @Override
  public void play(String uri) {
    player.play(uri);
  }

  @Override
  public void play(List<String> trackUris) {
    player.play(trackUris);
  }

  @Override
  public void play(PlayConfig playConfig) {
    player.play(playConfig);
  }

  @Override
  public void queue(String uri) {
    player.queue(uri);
  }

  @Override
  public void clearQueue() {
    player.clearQueue();
  }

  @Override
  public void pause() {
    player.pause();
  }

  @Override
  public void resume() {
    player.resume();
  }

  @Override
  public void skipToNext() {
    player.skipToNext();
  }

  @Override
  public void skipToPrevious() {
    player.skipToPrevious();
  }

  @Override
  public void seekToPosition(int positionInMs) {
    player.seekToPosition(positionInMs);
  }

  @Override
  public void setShuffle(boolean enabled) {
    player.setShuffle(enabled);
  }

  @Override
  public void setRepeat(boolean enabled) {
    player.setRepeat(enabled);
  }

  @Override
  public void getPlayerState(PlayerStateCallback callback) {
    player.getPlayerState(callback);
  }

  @Override
  public void setConnectivityStatus(Connectivity status) {
    player.setConnectivityStatus(status);
  }

  @Override
  public void addPlayerNotificationCallback(PlayerNotificationCallback callback) {
    player.addPlayerNotificationCallback(callback);
  }

  @Override
  public void removePlayerNotificationCallback(PlayerNotificationCallback callback) {
    player.removePlayerNotificationCallback(callback);
  }

  @Override
  public void onPlaybackEvent(int eventCode, PlayerState playerState) {
    player.onPlaybackEvent(eventCode, playerState);
  }

  @Override
  public void onPlaybackError(int errorCode, String errorDetails) {
    player.onPlaybackError(errorCode, errorDetails);
  }

  @Override
  public void onAudioFlush() {
    player.onAudioFlush();
  }
}
