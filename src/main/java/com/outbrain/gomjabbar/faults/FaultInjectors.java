package com.outbrain.gomjabbar.faults;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.outbrain.gomjabbar.config.Configuration;
import com.outbrain.gomjabbar.execution.CommandExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

  private FaultInjectors(final Collection<FaultInjector> faultInjectors) {
    Preconditions.checkArgument(!(faultInjectors == null || faultInjectors.isEmpty()), "faultInjectors must not be null or empty");
    faultInjectors.forEach(faultInjector -> this.faultInjectors.put(faultInjector.id(), faultInjector));
  }

  public static FaultInjectors defaultFaultInjectors(final Configuration configuration) {
    final CommandExecutor commandExecutor = createCommandExecutor(configuration.commandExecutorFactoryClass);

    final LinkedList<FaultInjector> faultInjectors = new LinkedList<>();
    faultInjectors.add(new DummyFault());
    faultInjectors.addAll(configBasedInjectors(configuration, commandExecutor));

    return new FaultInjectors(Lists.newArrayList(faultInjectors));
  }

  private static CommandExecutor createCommandExecutor(Class<?> commandExecutorFactoryClass) {
    try {
      final Method createCommandExecutorMethod = commandExecutorFactoryClass.getMethod("createCommandExecutor");
      return (CommandExecutor) createCommandExecutorMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(commandExecutorFactoryClass + " must implement a public static createCommandExecutor() method", e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("Failed to invoke " + commandExecutorFactoryClass + ".createCommandExecutor()", e);
    }
  }

  private static LinkedList<FaultInjector> configBasedInjectors(Configuration configuration, CommandExecutor commandExecutor) {
    LinkedList<FaultInjector> faultInjectors = new LinkedList<>();

    if (configuration != null) {
      if (configuration.scripts != null) {
        configuration.scripts.forEach(e -> faultInjectors.add(new FaultScriptInjector(commandExecutor, e)));
      }
      if (configuration.commands != null) {
        configuration.commands.forEach(e -> faultInjectors.add(new FaultCommandInjector(commandExecutor, e)));
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
