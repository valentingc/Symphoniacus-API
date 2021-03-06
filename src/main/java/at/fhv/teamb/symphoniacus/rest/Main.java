package at.fhv.teamb.symphoniacus.rest;

import at.fhv.teamb.symphoniacus.rest.configuration.jersey.ObjectMapperResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main entry class for REST API.
 * Help from
 * https://medium.com/@wakingrufus/minimal-java-rest-part-1-skeleton-w-grizzly-jersey-e596c36359c0
 * @author Valentin Goronjic
 */
public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);
    private static final String BASE_URI = "http://0.0.0.0:9005/";

    /**
     * Starts up the REST API.
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        ResourceConfig rc = new ResourceConfig();
        rc.register(ObjectMapperResolver.class);
        rc.register(JacksonFeature.class);
        rc.packages("at.fhv.teamb.symphoniacus.rest");

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        // Only to serve the static content in the /resources/public directory
        // Change the apiUrl in the enviroments.ts to 'http://127.0.0.1:9005/api'
        // Run ng build and copy the files from Symphoniacus-Web/dist/Symphoniacus-Web
        // in the public folder
        // HttpHandler httpHandler =
        //    new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "/public/");
        // httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/");



        try {
            httpServer.start();
            LOG.info("Server running on {}", BASE_URI);
            LOG.info("Symphoniacus API started.\nHit enter to stop it...");
            System.in.read();
        } catch (IOException e) {
            LOG.error("Error starting server: {}", e.getLocalizedMessage());
        }
    }
}
