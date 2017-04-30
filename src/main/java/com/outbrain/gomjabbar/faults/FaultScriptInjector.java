package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class FaultScriptInjector implements FaultInjector {

  private final RundeckCommandExecutor commandExecutor;
  private final String id;
  private final String description;
  private final String scriptUrl;
  private final String scriptArgs;
  private final String revertScriptUrl;
  private final String revertScriptArgs;

  public FaultScriptInjector(final RundeckCommandExecutor commandExecutor, final String id, final String description, final String scriptUrl, final String scriptArgs, final String revertScriptUrl, final String revertScriptArgs) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
    this.id = id;
    this.description = description;
    this.scriptUrl = scriptUrl;
    this.scriptArgs = scriptArgs;
    this.revertScriptUrl = revertScriptUrl;
    this.revertScriptArgs = revertScriptArgs;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    return execute(target, scriptUrl, scriptArgs);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    return revertScriptUrl == null ?
      ComposableFutures.fromValue("no revert script was provided") :
      execute(target, revertScriptUrl, revertScriptArgs);
  }

  private ComposableFuture<String> execute(final Target target, final String scriptUrl, final String scriptArgs) {
    final RundeckCommand command = RundeckCommand.Builder.forTarget(target.getHost()).buildScriptUrl(scriptUrl, scriptArgs);
    return commandExecutor.executeScriptByUrlAsync(command);
  }
}
