package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

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
  public String id() {
    return getClass().getName();
  }

  @Override
  public String description() {
    return "Runs a harmless shell command on remote targets - should take about 5 sec to complete";
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), "for i in `seq 1 5`; do echo $i; sleep 1; done\n");
    return commandExecutor.executeCommandAsync(command);
  }
}
