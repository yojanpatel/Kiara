package uk.co.yojan.kiara.server.resources;


import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.RefreshAccessTokenCredentials;
import uk.co.yojan.kiara.server.SpotifyApi;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/auth")
public class SpotifyAuth {

  /* To perform Authorization Code Flow for the Spotify APIs.
   *
   * A 'code' is retrieved from Spotify on initial log-in by the Client.
   * This code is then sent to the Kiara service /auth in a request.
   * Access and Refresh tokens are then requested by the Kiara backend from the Spotify backends.
   * These tokens are then sent back to the Client.
   * The client then stores the refresh tokens locally to be able to refresh the access token.
   */

  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAccessAndRefreshTokens(String code) {
    try {
      AuthorizationCodeCredentials cred = SpotifyApi.authorizationCodeGrant(code);
      return Response.ok().entity(cred).build();
    } catch (IOException e) {
      e.printStackTrace();
      return Response.serverError().build();
    } catch (WebApiException e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  /*
   * Get an access token from a refresh Token.
   */
  @POST
  @Path("/refresh")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticate(String refreshToken) {
    try {
      RefreshAccessTokenCredentials accessToken = SpotifyApi.getAccessTokenFromRefreshToken(refreshToken);
      return Response.ok().entity(accessToken).build();
    } catch (IOException e) {
      e.printStackTrace();
      return Response.serverError().build();
    } catch (WebApiException e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }
}
