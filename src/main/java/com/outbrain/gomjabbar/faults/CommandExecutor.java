package com.outbrain.gomjabbar.faults;

import com.outbrain.ob1k.concurrent.ComposableFuture;

/**
 * @author Eran Harel
 */
public interface CommandExecutor {

  ComposableFuture<String> executeCommandAsync(RemoteCommand command);

  ComposableFuture<String> executeScriptByUrlAsync(RemoteCommand command);
}
