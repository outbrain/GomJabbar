package com.outbrain.gomjabbar.faults;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
  public static FaultInjectors defaultFaultInjectors(Collection<FaultScript> scripts) {
    final String authToken = System.getProperty("com.outbrain.gomjabbar.rundeckAuthToken");
    final String runDeckHost = System.getProperty("com.outbrain.gomjabbar.rundeckHost");
    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);

    final LinkedList<FaultInjector> faultInjectors = new LinkedList<>();
    faultInjectors.add(new DummyRemoteFailureInjector(rundeckCommandExecutor));
    faultInjectors.add(new DummyFault());
    faultInjectors.add(new InitdStopper(rundeckCommandExecutor));
    faultInjectors.add(new SIGKILLer(rundeckCommandExecutor));

    if (scripts != null) {
      scripts.forEach(e -> faultInjectors.add(new FaultScriptInjector(rundeckCommandExecutor, e)));
    }

    return new FaultInjectors(Lists.newArrayList(faultInjectors));
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
