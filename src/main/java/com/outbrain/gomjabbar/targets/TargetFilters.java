package com.outbrain.gomjabbar.targets;

import com.outbrain.ob1k.consul.HealthInfoInstance;

import java.util.function.Predicate;

/**
 * @author Eran Harel
 */
public interface TargetFilters {

  Predicate<String> dcFilter();
  Predicate<String> moduleFilter();
  Predicate<HealthInfoInstance> instanceFilter();
}
