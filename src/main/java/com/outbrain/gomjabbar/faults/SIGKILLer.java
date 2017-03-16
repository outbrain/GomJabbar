package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class SIGKILLer implements FaultInjector {

  private final RundeckCommandExecutor commandExecutor;

  public SIGKILLer(final RundeckCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
  }

  @Override
  public String id() {
    return getClass().getName();
  }

  @Override
  public String description() {
    return "Brutally kills service instances";
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), String.format("sudo pkill -9 -f %s", target.getModule()));
    return commandExecutor.executeCommandAsync(command);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), String.format("sudo service %s start", target.getModule()));
    return commandExecutor.executeCommandAsync(command);
  }
}
