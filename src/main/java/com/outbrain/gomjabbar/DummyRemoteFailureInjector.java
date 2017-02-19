package com.outbrain.gomjabbar;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class DummyRemoteFailureInjector implements FaultInjector {

  private final RundeckCommandExecutor commandExecutor;

  public DummyRemoteFailureInjector(final RundeckCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
  }

  @Override
  public void injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), "for i in `seq 1 5`; do echo $i; sleep 1; done\n");
    commandExecutor.executeCommand(command);
  }
}
