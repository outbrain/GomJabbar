package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.Map;

/**
 * @author Eran Harel
 */
public interface GomJabbarService extends com.outbrain.ob1k.Service {
  ComposableFuture<Map<String, Target>> selectTarget();

  ComposableFuture<Map<String, String>> faultOptions();

  ComposableFuture<String> trigger(String targetToken, String faultId);
}
