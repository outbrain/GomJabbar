package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.execution.CommandExecutor;
import com.outbrain.gomjabbar.execution.RemoteCommand;
import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A configurable (shell) fault command injector.
 * @author Eran Harel
 */
public class FaultCommandInjector implements FaultInjector {

  private final CommandExecutor commandExecutor;
  private final FaultCommand faultCommand;

  FaultCommandInjector(final CommandExecutor commandExecutor, final FaultCommand faultCommand) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
    this.faultCommand = Objects.requireNonNull(faultCommand, "faultCommand must not be null");
  }

  @Override
  public String id() {
    return faultCommand.id;
  }

  @Override
  public String description() {
    return faultCommand.description;
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    return execute(target, faultCommand.command);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    return faultCommand.revertCommand == null ?
      ComposableFutures.fromValue("no revert script was provided") :
      execute(target, faultCommand.revertCommand);
  }

  private ComposableFuture<String> execute(final Target target, final String command) {
    final RemoteCommand remoteCommand = RemoteCommand.Builder.forTarget(target.getHost()).buildCommand(formatCommand(command, target));
    return commandExecutor.executeCommandAsync(remoteCommand);
  }

  private String formatCommand(final String command, final Target target) {
    Map<Object, Object> valuesMap = new HashMap<>();
    valuesMap.put("host", target.getHost());
    valuesMap.put("module", target.getModule());
    valuesMap.putAll(System.getProperties());

    return new StrSubstitutor(valuesMap).replace(command);
  }

}
