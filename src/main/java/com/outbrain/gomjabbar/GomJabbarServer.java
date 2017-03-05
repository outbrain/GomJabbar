package com.outbrain.gomjabbar;

import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.server.Server;
import com.outbrain.ob1k.server.builder.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.outbrain.ob1k.server.endpoints.EndpointMappingServiceBuilder.registerMappingService;

/**
 * @author Eran Harel
 */
public class GomJabbarServer {
  private static final Logger logger = LoggerFactory.getLogger(GomJabbarServer.class);

  public static final int PORT = 8080;
  public static final String CTX_PATH = "/gj";
  public static final String SERVICE_PATH = "/api";

  private Server server;

  public static void main(final String[] args) {
    new GomJabbarServer().start(PORT);
  }

  public void start(final int port) {
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
    return ServerBuilder.newBuilder()
      .contextPath(CTX_PATH)
      .configure(builder -> builder.usePort(port).requestTimeout(30, TimeUnit.SECONDS))
      .service(builder -> builder.register(new GomJabbarService(createFaultInjectors(), creteTargetsCollector(), new AuditLog()), SERVICE_PATH))
      .withExtension(registerMappingService("/endpoints"))
      .build();
  }

  // TODO externalize
  private TargetsCollector creteTargetsCollector() {
    final Set<String> excludedDCs = Collections.singleton("il");
    final Set<String> excludedModules = Collections.emptySet();
    final Set<String> includedServiceTypes = Collections.singleton("ob1k");

    return new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), new DefaultTargetsFilter(excludedDCs, excludedModules, includedServiceTypes));
  }

  // TODO externalize
  private FaultInjectors createFaultInjectors() {

    final String authToken = System.getProperty("com.outbrain.gomjabbar.rundeckAuthToken");
    final String runDeckHost = System.getProperty("com.outbrain.gomjabbar.rundeckHost");

    // OK, this should load it from somewhere later...
    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);
    final FaultInjector faultInjector = new DummyRemoteFailureInjector(rundeckCommandExecutor);
//    final FaultInjector faultInjector = new DummyFault();
//    final FaultInjector faultInjector = new InitdStopper(rundeckCommandExecutor);
    return new FaultInjectors(Collections.singleton(faultInjector));
  }

}
