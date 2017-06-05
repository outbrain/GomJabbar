package com.outbrain.gomjabbar.execution;

import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * Defines the common API all remote command executors must implement.
 * @author Eran Harel
 */
public interface CommandExecutor {

  /**
   * Executes the given remote command
   * @param command the command data
   * @return a future command output
   */
  ComposableFuture<String> executeCommandAsync(RemoteCommand command);

  /**
   * Executes the given remote script.
   * Scripts are usually downloaded from the specified URL) and executed on the remote targets, depending on the implementation.
   * @param command the script data
   * @return a future script output
   */
  ComposableFuture<String> executeScriptByUrlAsync(RemoteCommand command);
}
