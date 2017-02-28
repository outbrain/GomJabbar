package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * @author Eran Harel
 */
public interface FaultInjector {
  String id();
  String description();
  ComposableFuture<String> injectFailure(final Target target);
}
