package com.outbrain.gomjabbar;

import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Eran Harel
 */
public class Target {
  private final String host;
  private final String module;
  private final String serviceType;
  private final Set<String> tags;


  public Target(final String host, final String module, final String serviceType, final Set<String> tags) {
    this.host = Objects.requireNonNull(host, "host must not be null");
    this.module = Objects.requireNonNull(module, "module must not be null");
    this.serviceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
    this.tags = Objects.requireNonNull(new HashSet<>(tags), "tags must not be null");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("module", module)
      .add("host", host)
      .add("serviceType", serviceType)
      .add("tags", tags)
      .toString();
  }

  public String getHost() {
    return host;
  }

  public String getModule() {
    return module;
  }

  public String getServiceType() {
    return serviceType;
  }

  public Set<String> getTags() {
    return Collections.unmodifiableSet(tags);
  }
}
