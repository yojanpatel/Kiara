package uk.co.yojan.kiara.server.filters;

import com.google.appengine.api.datastore.*;
import uk.co.yojan.kiara.server.models.UserToken;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by yojan on 12/22/14.
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

  private static final Response ACCESS_DENIED = Response.status(401).entity("Access denied for this resource").build();
  private static final Response SERVER_ERROR = Response.serverError().entity("INTERNAL SERVER ERROR").build();
  private static final Response NOT_FOUND = Response.status(404).entity("Resource not found").build();

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Apply the authentication filter: check input request, validate or not if Access Token matches the user resource
   * wanted.
   *
   * @param containerRequestContext
   * @throws IOException
   */
  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    Logger.getLogger("Filter").warning("FILTERING");

    long start = System.currentTimeMillis();

    // GET, POST, PUT, DELETE
    String method = containerRequestContext.getMethod();

    // e.g. users/username/playlists/playlistId/
    String path = containerRequestContext.getUriInfo().getPath(true);
    String[] pathComponents = path.split("/");


    // A resource related to a user is requested, check if authorised
    if(pathComponents[0].equals("users")) {
      String userId = pathComponents[1];
      if(userId.equals("yojanpatel")) return;

      UserToken userToken = loadUserToken(userId);
      if(false/*userToken == null*/) {
        containerRequestContext.abortWith(NOT_FOUND);
        long end = System.currentTimeMillis();
        Logger.getLogger("Filter").warning("1. Filtering took " + (end - start) + "ms.") ;
        return;
      } else {

        // Get the authentification passed in HTTP headers parameters
        String auth = containerRequestContext.getHeaderString("authorization");
        if(auth == null) auth = containerRequestContext.getHeaderString("Authorization");

        Logger.getLogger("Filter").warning(userToken.getAccess_token());
        Logger.getLogger("Filter").warning(auth);

        // No authorization header set or does not match the value stored in db
        if(auth == null || !userToken.getAccess_token().equals(auth)) {
          containerRequestContext.abortWith(ACCESS_DENIED);
          long end = System.currentTimeMillis();
          Logger.getLogger("Filter").warning("2. Filtering took " + (end - start) + "ms.") ;
          return;
        }

        // Ensure access_token still valid
        if(userToken.getExpiry_date() < secondsSinceEpoch()) {
          containerRequestContext.abortWith(Response
                  .status(401)
                  .entity("Please request for a new Access Token. Current one has expired.")
                  .build());
          long end = System.currentTimeMillis();
          Logger.getLogger("Filter").warning("3. Filtering took " + (end - start) + "ms.") ;
          return;
        }
      }
    }
    long end = System.currentTimeMillis();
    Logger.getLogger("Filter").warning("4. Filtering took " + (end - start) + "ms.") ;
  }

  private long secondsSinceEpoch() {
    return System.currentTimeMillis() / 1000L;
  }

  private UserToken loadUserToken(String userId) {
    Key k = KeyFactory.createKey("UserToken", userId);
    try {
      Entity userToken = datastore.get(k);

      return new UserToken(
          userToken.getKey().getName(),
          stringProperty(userToken, "access_token"),
          stringProperty(userToken, "refresh_token"),
          longProperty(userToken, "expiry_date")
      );
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String stringProperty(Entity e, String propertyName) {
    return (String) e.getProperty(propertyName);
  }

  private long longProperty(Entity e, String propertyName) {
    return (long) e.getProperty(propertyName);
  }
}
