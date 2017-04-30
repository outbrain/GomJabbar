package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class Fault {
  public final Target target;
  public final String faultInjectorId;

  Fault() {
    target = null;
    faultInjectorId = null;
  }

  public Fault(final Target target, final String faultInjectorId) {
    this.target = Objects.requireNonNull(target, "target must not be null");
    this.faultInjectorId = Objects.requireNonNull(faultInjectorId, "faultInjectorId must not be null");
  }
}
