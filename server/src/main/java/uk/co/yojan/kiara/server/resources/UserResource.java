package uk.co.yojan.kiara.server.resources;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Random;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * User Resource.
 *
 * Provides implementation for the UserResource interface, involving the HTTP methods.
 * These are the methods exposed via the REST API.
 */
@Path("/users")
public class UserResource {

  private static Logger log = Logger.getLogger(UserResource.class.getName());

  @POST
  @Path("/authenticate")
  public User authenticate(@FormParam("username") String username,
                           @FormParam("password") String password) {
    return null;
  }

  @GET
  public String getd() {
    SongAnalysis s = EchoNestApi.getSongAnalysis("1otfHLXHeYBw3qLK926rCh");
    return s.getTitle();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public User get(@PathParam("id") String id) {
    Key key = Key.create(User.class, id);
    return (User)ofy().load().key(key).now();
  }

  @DELETE
  @Path("/{id}")
  public Response delete(@PathParam("id") String id) {
    Key key = Key.create(User.class, id);
    ofy().delete().key(key).now();
    return Response.ok().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response update(@PathParam("id") String id, User item) {
    User loaded = ofy().load().key(Key.create(User.class, id)).now();
    if(loaded == null) {
      return Response.noContent().build();
    } else {
      loaded.copyFrom(item);
      loaded.incrementCounter();
      ofy().save().entity(loaded); // async
      return Response.ok().entity(loaded).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Context UriInfo uri, User item) {

    Random r = new Random();
    double p = r.nextDouble();
    boolean test;
    test = (0.4 < p);
    item.setTest(test);


    ofy().save().entity(item).now();
    URI userURI = UriBuilder.fromUri(uri.getRequestUri()).path(item.getId()).build();
    log.info("Created new user at " + userURI.getRawPath());
    return Response.created(userURI).build();
  }

  @POST
  @Path("/{id}/test/{bool}")
  public Response setTest(@PathParam("id") String userId, @PathParam("bool") int test) {
    User user = OfyUtils.loadUser(userId).now();
    user.setTest(test == 37);
    ofy().save().entity(user).now();
    return Response.ok().build();
  }
}
