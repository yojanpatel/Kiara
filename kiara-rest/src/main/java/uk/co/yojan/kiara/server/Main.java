package uk.co.yojan.kiara.server;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import uk.co.yojan.kiara.server.models.User;
import uk.co.yojan.kiara.server.serializers.PlaylistSerializer;
import uk.co.yojan.kiara.server.serializers.SongSerializer;
import uk.co.yojan.kiara.server.serializers.UserSerializer;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;
/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application. The Java
 * command that is used for launching should fire this main method.
 */
public class Main {
    private static Logger log = Logger.getLogger("MAIN");

    private static void addUser() {
      User yojan = User.newInstanceFromSpotify("yojanpatel");
      ofy().save().entity(yojan).now();
    }

  private static ObjectMapper getJsonProvider() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule testModule = new SimpleModule("testModule", new Version(1,0,0,null))
        .addSerializer(new UserSerializer())
        .addSerializer(new PlaylistSerializer())
        .addSerializer(new SongSerializer());


    mapper.registerModule(testModule);

    return mapper;
  }

    public static void main(String[] args) throws Exception{
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        final Server server = new Server(Integer.valueOf(webPort));
        final WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        server.setHandler(root);
        server.start();
        server.join();
    }
}
