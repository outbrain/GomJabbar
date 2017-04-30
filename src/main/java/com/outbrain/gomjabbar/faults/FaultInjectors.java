package com.outbrain.gomjabbar.faults;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class FaultInjectors {

  private final Map<String, FaultInjector> faultInjectors = new HashMap<>();

  public FaultInjectors(final Collection<FaultInjector> faultInjectors) {
    Preconditions.checkArgument(!(faultInjectors == null || faultInjectors.isEmpty()), "faultInjectors must not be null or empty");
    faultInjectors.forEach(faultInjector -> this.faultInjectors.put(faultInjector.id(), faultInjector));
  }

  // TODO externalize
  public static FaultInjectors defaultFaultInjectors() {
    final String authToken = System.getProperty("com.outbrain.gomjabbar.rundeckAuthToken");
    final String runDeckHost = System.getProperty("com.outbrain.gomjabbar.rundeckHost");

    // OK, this should load it from somewhere later...
    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);
    final FaultInjector dummyRemoteFailureInjector = new DummyRemoteFailureInjector(rundeckCommandExecutor);
    final FaultInjector dummyLocalFault = new DummyFault();
    final FaultInjector gracefulShutdownInjector = new InitdStopper(rundeckCommandExecutor);
    final FaultInjector gracelessShutdownInjector = new SIGKILLer(rundeckCommandExecutor);
    final TraficController traficControllerInjector = new TraficController(rundeckCommandExecutor, 10, 300 ) ;
//    final FaultInjector echo = new FaultScriptInjector(rundeckCommandExecutor, "echo", "echo stuff", "https://gist.githubusercontent.com/eranharel/077730def256486e5e19e2205ef8b695/raw/023145fde84b330818e611b9b4ad1de22a444675/echoargs.sh", "arg1 arg2", null, null);
    return new FaultInjectors(Lists.newArrayList(dummyRemoteFailureInjector, dummyLocalFault, gracefulShutdownInjector, gracelessShutdownInjector, traficControllerInjector));
  }

  public FaultInjector selectFaultInjector() {
    return faultInjectors.entrySet()
      .stream()
      .skip(ThreadLocalRandom.current().nextInt(faultInjectors.size()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Unexpected bounds...")).getValue();
  }

  public Map<String, String> options() {
    return faultInjectors.entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, fi -> fi.getValue().description()));
  }

  public FaultInjector getFaultInjector(final String id) {
    return faultInjectors.get(id);
  }
}
