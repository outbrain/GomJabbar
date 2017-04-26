package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Eran Harel
 */
public abstract class AbstractCommandFaultInjector implements FaultInjector {

  private final RundeckCommandExecutor commandExecutor;

  public AbstractCommandFaultInjector(final RundeckCommandExecutor commandExecutor) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
  }

  protected abstract String formatCommand(final Target target);
  protected abstract Optional<String> formatRevertCommand(final Target target);

  @Override
  public String id() {
    return getClass().getName();
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), formatCommand(target));
    return commandExecutor.executeCommandAsync(command);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    return formatRevertCommand(target).map(cmd -> commandExecutor.executeCommandAsync(new RundeckCommand(target.getHost(), cmd)))
      .orElse(ComposableFutures.fromValue("No revert command was given"));
  }
}
