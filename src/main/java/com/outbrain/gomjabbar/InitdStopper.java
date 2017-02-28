package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class InitdStopper implements FaultInjector {

  private final RundeckCommandExecutor commandExecutor;

  public InitdStopper(final RundeckCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
  }

  @Override
  public String id() {
    return getClass().getName();
  }

  @Override
  public String description() {
    return "Gracefully shuts down services";
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), String.format("sudo service %s stop", target.getModule()));
    return commandExecutor.executeCommandAsync(command);
  }
}
