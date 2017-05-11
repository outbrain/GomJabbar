package com.outbrain.gomjabbar.targets;

import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Eran Harel
 */
public class Target {
  private final String host;
  private final String module;
  private final Set<String> tags;
  private final int instanceCount;

  Target() {
    // bahhhhhh
    this("", "", 0, Collections.emptySet());
  }

  public Target(final String host, final String module, final int instanceCount, final Set<String> tags) {
    this.host = Objects.requireNonNull(host, "host must not be null");
    this.module = Objects.requireNonNull(module, "module must not be null");
    this.tags = new HashSet<>(Optional.of(tags).orElse(new HashSet<>()));
    this.instanceCount = instanceCount;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("module", module)
      .add("host", host)
      .add("instanceCount", instanceCount)
      .add("tags", tags)
      .toString();
  }

  public String getHost() {
    return host;
  }

  public String getModule() {
    return module;
  }


  public Set<String> getTags() {
    return Collections.unmodifiableSet(tags);
  }

  public int getInstanceCount() {
    return instanceCount;
  }
}
