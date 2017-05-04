package com.outbrain.gomjabbar.config;

import com.outbrain.gomjabbar.faults.FaultScript;
import com.outbrain.gomjabbar.targets.TargetFilters;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Eran Harel
 */
public class Configuration {
  public final TargetFilters targetFilters;
  public final Collection<FaultScript> scripts;

  public Configuration(TargetFilters targetFilters, Collection<FaultScript> scripts) {
    this.targetFilters = targetFilters;
    this.scripts = scripts == null ? Collections.emptyList() : Collections.unmodifiableCollection(scripts);
  }
}
