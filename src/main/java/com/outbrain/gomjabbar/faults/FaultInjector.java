package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * @author Eran Harel
 */
public interface FaultInjector {
  String id();
  String description();
  ComposableFuture<String> injectFailure(final Target target);
  ComposableFuture<String> revertFailure(final Target target);
}
