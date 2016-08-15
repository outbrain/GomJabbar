package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * @author Eran Harel
 */
public interface TargetsCollector {
  String UNDEFINED = "Undefined";

  ComposableFuture<Target> chooseTarget();
}
