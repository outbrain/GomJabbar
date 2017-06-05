package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * FaultInjectors injects / reverts a failure on the specified targets.
 * A fault injector must have a unique ID, and preferably a clear description.
 * @author Eran Harel
 */
public interface FaultInjector {
  /**
   * @return a unique injector ID
   */
  String id();

  /**
   * @return a human readable description of the injected failure
   */
  String description();

  /**
   * injects a failure on the specified targets.
   * @param target specifies the injection target
   * @return a future execution output
   */
  ComposableFuture<String> injectFailure(final Target target);

  /**
   * reverts a failure execution on the specified targets.
   * @param target specifies the injection target
   * @return a future execution output
   */
  ComposableFuture<String> revertFailure(final Target target);
}
