package com.outbrain.gomjabbar.targets;

import com.outbrain.ob1k.consul.HealthInfoInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Eran Harel
 */
public class DefaultTargetsFilter implements TargetFilters {

  private final Set<String> includeDCs;
  private final Set<String> excludeDCs;
  private final Set<String> includeModules;
  private final Set<String> excludeModules;
  private final Set<String> includeTags;
  private final Set<String> excludeTags;

  public DefaultTargetsFilter(final Set<String> includeDCs, final Set<String> excludeDCs,
                              final Set<String> includeModules, final Set<String> excludeModules,
                              final Set<String> includeTags, final Set<String> excludeTags) {
    this.excludeDCs = new HashSet<>(excludeDCs);
    this.includeDCs = new HashSet<>(includeDCs);
    this.excludeModules = new HashSet<>(excludeModules);
    this.includeModules = new HashSet<>(includeModules);
    this.includeTags = new HashSet<>(includeTags);
    this.excludeTags = new HashSet<>(excludeTags);
  }

  @Override
  public Predicate<String> dcFilter() {
    return dc -> isIncludedAndNotExcluded(dc, includeDCs, excludeDCs);
  }

  @Override
  public Predicate<String> moduleFilter() {
    return module -> isIncludedAndNotExcluded(module, includeModules, excludeModules);
  }

  @Override
  public Predicate<HealthInfoInstance> instanceFilter() {
    return instance -> {
      final Set<String> serviceTags = Optional.ofNullable(instance.Service.Tags).orElseGet(Collections::emptySet);
      return Collections.disjoint(serviceTags, excludeTags)
        && (includeTags.isEmpty() || !Collections.disjoint(serviceTags, includeTags))
        && instance.Checks.stream().allMatch(check -> "passing".equals(check.Status));
    };
  }

  private boolean isIncludedAndNotExcluded(final String e, final Collection<String> includes, final Collection<String> excludes) {
    return !excludes.contains(e) && (includes.isEmpty() || includes.contains(e));
  }
}
