package io.neocdtv.jetty.base;

import io.swagger.jaxrs.config.BeanConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jboss.weld.environment.servlet.Listener;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;

/**
 * Main.
 *
 * @author xix
 * @since 22.12.17
 */
public class Main {

  public static void main(String[] args) throws Exception {

    Server server = new Server(Constants.NETWORK_PORT);
    WebAppContext context = configureWebContext(server);

    configureCdi(context);
    final ResourceConfig jerseyConfig = configureJersey();
    configureSwagger(jerseyConfig);
    configureServlet(context, jerseyConfig);

    server.start();
    System.out.println(String.format("Application available at %s://%s:%s%s",
        Constants.NETWORK_PROTOCOL_HTTP,
        Constants.NETWORK_HOST,
        Constants.NETWORK_PORT,
        Constants.CONTEXT_PATH));
    System.out.println(String.format("Swagger available at %s://%s:%s%s%s/swagger.json",
        Constants.NETWORK_PROTOCOL_HTTP,
        Constants.NETWORK_HOST,
        Constants.NETWORK_PORT,
        Constants.CONTEXT_PATH,
        Constants.PATH_BASE_REST));
    System.out.println("Websocket connection available at - TODO");
    server.join();
  }

  private static WebAppContext configureWebContext(Server server) throws IOException {
    WebAppContext context = new WebAppContext();
    context.setContextPath(Constants.CONTEXT_PATH);

    context.setResourceBase(".");
    context.setResourceBase(new ClassPathResource("static").getURI().toString());
    context.setClassLoader(Thread.currentThread().getContextClassLoader());

    server.setHandler(context);
    return context;
  }

  private static ResourceConfig configureJersey() {
    ResourceConfig config = new ResourceConfig();
    config.packages(Constants.RESOURCE_PACKAGE);
    return config;
  }

  private static void configureSwagger(ResourceConfig config) {

    config.register(io.swagger.jaxrs.listing.ApiListingResource.class);
    config.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion(Constants.APP_VERSION);
    beanConfig.setSchemes(new String[]{Constants.NETWORK_PROTOCOL_HTTP});
    beanConfig.setHost(getHost());
    beanConfig.setBasePath(getBasePath());
    beanConfig.setResourcePackage(Constants.RESOURCE_PACKAGE);
    beanConfig.setScan(true);

  }

  private static void configureServlet(WebAppContext context, ResourceConfig jerseyConfig) {
    ServletHolder servlet = new ServletHolder(new ServletContainer(jerseyConfig));
    context.addServlet(servlet, getResourcePath());
  }

  private static void configureCdi(WebAppContext context) {
    Listener listener = new Listener();
    context.addEventListener(listener);
    // 18.3.2.2. Binding BeanManager to JNDI, is JDNI by default enabled on jetty?
    //context.addEventListener(new BeanManagerResourceBindingListener());
  }

  private static String getBasePath() {
    return String.format("/%s", Constants.PATH_BASE_REST);
  }

  private static String getHost() {
    return String.format("%s:%s", Constants.NETWORK_HOST, Constants.NETWORK_PORT);
  }

  private static String getResourcePath() {
    return String.format("/%s/*", Constants.PATH_BASE_REST);
  }
}