package com.outbrain.gomjabbar;

/**
 * @author Eran Harel
 */
public interface FaultInjector {
  String id();
  void injectFailure(final Target target);
}
