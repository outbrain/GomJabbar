package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

/**
 * A dummy failure used for testing, simulations, and debugging
 * @author Eran Harel
 */
public class DummyFault implements FaultInjector {

  @Override
  public String id() {
    return getClass().getName();
  }

  @Override
  public String description() {
    return "I'm just used to debug the flow ;)";
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    System.out.println("Failing " + target);
    return ComposableFutures.fromValue("Let's pretend I did it K? target=" + target);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    System.out.println("Reverting " + target);
    return ComposableFutures.fromValue("Let's pretend I reverted it K? target=" + target);
  }
}
