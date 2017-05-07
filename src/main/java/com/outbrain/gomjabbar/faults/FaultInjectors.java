package com.outbrain.gomjabbar.faults;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.outbrain.gomjabbar.config.Configuration;

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

  public static FaultInjectors defaultFaultInjectors(final Configuration configuration) {
    final String authToken = System.getProperty("com.outbrain.gomjabbar.rundeckAuthToken");
    final String runDeckHost = System.getProperty("com.outbrain.gomjabbar.rundeckHost");
    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);

    final LinkedList<FaultInjector> faultInjectors = new LinkedList<>();
    faultInjectors.add(new DummyFault());
    faultInjectors.addAll(configBasedInjectors(configuration, rundeckCommandExecutor));

    return new FaultInjectors(Lists.newArrayList(faultInjectors));
  }

  private static LinkedList<FaultInjector> configBasedInjectors(Configuration configuration, RundeckCommandExecutor rundeckCommandExecutor) {
    LinkedList<FaultInjector> faultInjectors = new LinkedList<>();

    if (configuration != null) {
      if (configuration.scripts != null) {
        configuration.scripts.forEach(e -> faultInjectors.add(new FaultScriptInjector(rundeckCommandExecutor, e)));
      }
      if (configuration.commands != null) {
        configuration.commands.forEach(e -> faultInjectors.add(new FaultCommandInjector(rundeckCommandExecutor, e)));
      }
    }

    return faultInjectors;
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
