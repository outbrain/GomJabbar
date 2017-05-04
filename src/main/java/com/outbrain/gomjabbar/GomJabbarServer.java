package com.outbrain.gomjabbar;

import com.outbrain.gomjabbar.audit.AuditLog;
import com.outbrain.gomjabbar.config.ConfigParser;
import com.outbrain.gomjabbar.config.Configuration;
import com.outbrain.gomjabbar.faults.FaultInjectors;
import com.outbrain.gomjabbar.targets.ConsulTargetsCache;
import com.outbrain.gomjabbar.targets.TargetsCollector;
import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.server.Server;
import com.outbrain.ob1k.server.builder.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.outbrain.ob1k.server.endpoints.EndpointMappingServiceBuilder.registerMappingService;

/**
 * @author Eran Harel
 */
public class GomJabbarServer {
  private static final Logger logger = LoggerFactory.getLogger(GomJabbarServer.class);

  private static final int PORT = 8080;
  private static final String CTX_PATH = "/gj";
  private static final String SERVICE_PATH = "/api";

  private Server server;
  private final Configuration configuration;

  public static void main(final String[] args) {
    new GomJabbarServer().start(PORT);
  }

  private GomJabbarServer() {
    final URL configFileUrl = resolveConfigFileUrl();
    configuration = ConfigParser.parseConfiguration(configFileUrl);
  }

  private void start(final int port) {
    server = buildServer(port);
    server.start();
    logger.info("## {} is started on port: {} ##", getClass().getSimpleName(), port);
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  private Server buildServer(final int port) {
    final long requestTimeout = 30;
    return ServerBuilder.newBuilder()
      .contextPath(CTX_PATH)
      .configure(builder -> builder.usePort(port).requestTimeout(requestTimeout, TimeUnit.SECONDS))
      .service(builder -> builder.register(new GomJabbarServiceImpl(FaultInjectors.defaultFaultInjectors(configuration.scripts), creteTargetsCollector(), new AuditLog()), SERVICE_PATH))
      .withExtension(registerMappingService("/endpoints"))
      .build();
  }

  private TargetsCollector creteTargetsCollector() {
    return new ConsulTargetsCache(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), configuration.targetFilters);
//    return new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), configuration.targetFilters);
  }

  private URL resolveConfigFileUrl() {
    final String configFileUrl = System.getProperty("com.outbrain.gomjabbar.configFileUrl");
    try {
      return new URL(configFileUrl);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Failed to parse config file url: " + configFileUrl, e);
    }
  }

}
