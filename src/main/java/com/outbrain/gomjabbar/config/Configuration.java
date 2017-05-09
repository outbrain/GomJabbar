package com.outbrain.gomjabbar.config;

import com.outbrain.gomjabbar.faults.FaultCommand;
import com.outbrain.gomjabbar.faults.FaultScript;
import com.outbrain.gomjabbar.targets.TargetFilters;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Eran Harel
 */
public class Configuration {
  public final TargetFilters targetFilters;
  public final Collection<FaultScript> scripts;
  public final Collection<FaultCommand> commands;
  public final Class<?> commandExecutorFactoryClass;

  Configuration(TargetFilters targetFilters, Collection<FaultScript> scripts, final Collection<FaultCommand> commands, Class<?> commandExecutorFactoryClass) {
    this.targetFilters = targetFilters;
    this.scripts = scripts == null ? Collections.emptyList() : Collections.unmodifiableCollection(scripts);
    this.commands = commands == null ? Collections.emptyList() : Collections.unmodifiableCollection(commands);
    this.commandExecutorFactoryClass = commandExecutorFactoryClass;
  }
}
