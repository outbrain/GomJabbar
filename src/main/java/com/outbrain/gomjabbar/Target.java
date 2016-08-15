package com.outbrain.gomjabbar;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class Target {
  private final String host;
  private final String module;
  private final String serviceType;


  public Target(final String host, final String module, final String serviceType) {
    this.host = Objects.requireNonNull(host, "host must not be null");
    this.module = Objects.requireNonNull(module, "module must not be null");
    this.serviceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("module", module).add("host", host).add("serviceType", serviceType).toString();
  }
}
