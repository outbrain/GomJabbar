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

  public Configuration(TargetFilters targetFilters, Collection<FaultScript> scripts, final Collection<FaultCommand> commands) {
    this.targetFilters = targetFilters;
    this.scripts = scripts == null ? Collections.emptyList() : Collections.unmodifiableCollection(scripts);
    this.commands = commands == null ? Collections.emptyList() : Collections.unmodifiableCollection(commands);
  }
}
