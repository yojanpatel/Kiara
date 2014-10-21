package uk.co.yojan.kiara.client;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;
import uk.co.yojan.kiara.client.data.AuthorizationCodeGrant;
import uk.co.yojan.kiara.client.data.RefreshAccessToken;

public interface SpotifyAuthInterface {

  @POST("/swap")
  public AuthorizationCodeGrant authorizeCode(@Body String code);

  @POST("/refresh")
  public RefreshAccessToken refreshAccessToken(@Body String userId);

  @POST("/swap")
  public void authorizeCode(@Body String code,
                            Callback<AuthorizationCodeGrant> cb);

  @POST("/refresh")
  public void refreshAccessToken(@Body String userId,
                                 Callback<RefreshAccessToken> cb);

}
