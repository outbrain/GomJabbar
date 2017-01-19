package com.outbrain.gomjabbar;

/**
 * @author Eran Harel
 */
public interface FaultInjector {
  void injectFailure(final Target target);
}
