package uk.co.yojan.kiara.server.resources;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.echonest.data.SongData;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

//import retrofit.converter.GsonConverter;

/**
 * Song Resource.
 *
 * Contains the REST methods related to a Song instance for a given user.
 *
 */
@Path("/songs")
public class SongResource {
  private static Logger log = Logger.getLogger(Song.class.getName());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Song> getAll() {
    throw new NotSupportedException();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/data")
  public SongAnalysis getSongMetaData(@PathParam("id") String id) {
    return EchoNestApi.getSongMetaData(id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/data/analysis")
  public SongData getSongAnalysis(@PathParam("id") String id) {
    return EchoNestApi.getSongAnalysis(id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/search")
  public String searchSong(@QueryParam("artist") String artist,
                           @QueryParam("title") String song) {
    return EchoNestApi.searchSong(artist, song);
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Song get(@PathParam("id") String spotifyId) {
    return ofy().load().key(Key.create(Song.class, spotifyId)).now();
  }

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response delete(/*@PathParam("user_id") String userId, */@PathParam("id") Long id) {
//    User u = ofy().load().key(Key.create(User.class, userId)).now();
//    if(u.hasSong(id)) {
//      u.removeSong(id);
//      return Response.ok().build();
//    }
//    return Response.noContent().build();
    throw new NotSupportedException("Cannot delete a song yet cf. Issue/2");
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response update(@PathParam("id") Long id, Song item) {
    Song loaded = ofy().load().key(Key.create(Song.class, id)).now();
    if(loaded == null) {
      return Response.noContent().build();
    } else {
      loaded.copyFrom(item);
      ofy().save().entity(loaded); // async
      return Response.ok().entity(loaded).build();
    }
  }

//  @POST
//  @Consumes(MediaType.APPLICATION_JSON)
//  public Response create(Song item, @Context UriInfo uri) {
//    if(ofy().load().key(Key.create(Song.class, item.getId())).now() == null) {
//      ofy().save().entity(item).now();
//      URI songURI = UriBuilder.fromUri(uri.getRequestUri()).path(item.getId().toString()).build();
//      return Response.created(songURI).entity(item).build();
//    } else {
//      URI songURI = UriBuilder.fromUri(uri.getRequestUri()).path(item.getId().toString()).build();
//      return Response.ok().location(songURI).build();
//    }
//  }
//
//  @POST
//  @Consumes(MediaType.TEXT_PLAIN)
//  @Produces(MediaType.APPLICATION_JSON)
//  public Response create(String spotifyId,
//                         @PathParam("user_id") String userId,
//                         @Context UriInfo uri) {
//    User u = ofy().load().key(Key.create(User.class, userId)).now();
//    Song newSong;
//    try {
//      newSong = u.addSong(spotifyId);
//    } catch (Exception e) {
//      return Response.serverError().build();
//    }
//
//    // null - song already exists in user's library.
//    if(newSong == null) {
//      URI songURI = UriBuilder.fromUri(uri.getRequestUri()).path(u.getIdFromSpotifyId(spotifyId).toString()).build();
//      return Response.notModified().location(songURI).build();
//    } else {
//      URI songURI = UriBuilder.fromUri(uri.getRequestUri()).path(newSong.getId().toString()).build();
//      return Response.created(songURI).entity(newSong).build();
//    }
//  }
}