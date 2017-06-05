package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.execution.CommandExecutor;
import com.outbrain.gomjabbar.execution.RemoteCommand;
import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

import java.util.Objects;

/**
 * A configurable fault script injector.
 * Scripts are usually downloaded and executed on the remote targets, depending on the command executor implementation.
 * @author Eran Harel
 */
public class FaultScriptInjector implements FaultInjector {

  private final CommandExecutor commandExecutor;
  private final FaultScript faultScript;

  FaultScriptInjector(final CommandExecutor commandExecutor, final FaultScript faultScript) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
    this.faultScript = Objects.requireNonNull(faultScript, "faultScript must not be null");
  }

  @Override
  public String id() {
    return faultScript.id;
  }

  @Override
  public String description() {
    return faultScript.description;
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    return execute(target, faultScript.scriptUrl, faultScript.scriptArgs);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    return faultScript.revertScriptUrl == null ?
      ComposableFutures.fromValue("no revert script was provided") :
      execute(target, faultScript.revertScriptUrl, faultScript.revertScriptArgs);
  }

  private ComposableFuture<String> execute(final Target target, final String scriptUrl, final String scriptArgs) {
    final RemoteCommand command = RemoteCommand.Builder.forTarget(target.getHost()).buildScriptUrl(scriptUrl, scriptArgs);
    return commandExecutor.executeScriptByUrlAsync(command);
  }
}
