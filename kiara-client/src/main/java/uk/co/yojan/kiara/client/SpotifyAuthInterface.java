package uk.co.yojan.kiara.client;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Query;
import uk.co.yojan.kiara.client.data.AuthorizationCodeGrant;
import uk.co.yojan.kiara.client.data.RefreshAccessToken;

public interface SpotifyAuthInterface {

  @POST("/swap")
  public AuthorizationCodeGrant authorizeCode(@Query("code") String code);

  @POST("/refresh")
  public RefreshAccessToken refreshAccessToken(@Query("user") String userId);

  @POST("/swap")
  public void authorizeCode(@Query("code") String code,
                            Callback<AuthorizationCodeGrant> cb);

  @POST("/refresh")
  public void refreshAccessToken(@Query("user") String userId,
                                 Callback<RefreshAccessToken> cb);

}
