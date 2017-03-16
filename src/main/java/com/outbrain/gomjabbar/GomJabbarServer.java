package com.outbrain.gomjabbar;

import com.outbrain.gomjabbar.audit.AuditLog;
import com.outbrain.gomjabbar.config.ConfigParser;
import com.outbrain.gomjabbar.faults.FaultInjectors;
import com.outbrain.gomjabbar.targets.ConsulBasedTargetsCollector;
import com.outbrain.gomjabbar.targets.TargetFilters;
import com.outbrain.gomjabbar.targets.TargetsCollector;
import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.server.Server;
import com.outbrain.ob1k.server.builder.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static void main(final String[] args) {
    new GomJabbarServer().start(PORT);
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
      .service(builder -> builder.register(new GomJabbarServiceImpl(FaultInjectors.defaultFaultInjectors(), creteTargetsCollector(), new AuditLog()), SERVICE_PATH))
      .withExtension(registerMappingService("/endpoints"))
      .build();
  }

  private TargetsCollector creteTargetsCollector() {
    // TODO this should be passed as a system property
    final URL configFileUrl = ConfigParser.class.getClassLoader().getResource("config.yaml");
    final TargetFilters targetFilters = ConfigParser.parseConfiguration(configFileUrl);
    return new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), targetFilters);
  }

}
