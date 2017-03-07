package com.outbrain.gomjabbar.targets;

import com.outbrain.ob1k.consul.HealthInfoInstance;
import com.outbrain.ob1k.consul.TagsUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Eran Harel
 */
public class DefaultTargetsFilter implements TargetFilters {

  private final Set<String> includeServiceTypes;
  private final Set<String> excludeDCs;
  private final Set<String> excludeModules;

  public DefaultTargetsFilter(final Set<String> excludeDCs, final Set<String> excludeModules, final Set<String> includeServiceTypes) {
    this.excludeDCs = new HashSet<>(excludeDCs);
    this.excludeModules = new HashSet<>(excludeModules);
    this.includeServiceTypes = new HashSet<>(includeServiceTypes);
  }

  @Override
  public Predicate<String> dcFilter() {
    return dc -> !excludeDCs.contains(dc);
  }

  @Override
  public Predicate<String> moduleFilter() {
    return module -> !excludeModules.contains(module);
  }

  @Override
  public Predicate<HealthInfoInstance> instanceFilter() {
    return instance ->
      includeServiceTypes.contains(TagsUtil.extractTag(instance.Service.Tags, "servicetype")) &&
      instance.Checks.stream().allMatch(check -> "passing".equals(check.Status));
  }
}
