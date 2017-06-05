package com.outbrain.gomjabbar;

import com.outbrain.gomjabbar.faults.Fault;
import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.Map;

/**
 * REST API endpoints
 * @author Eran Harel
 */
public interface GomJabbarService extends com.outbrain.ob1k.Service {

  /**
   * Selects and returns a random target and a token to be used for injecting failures on that target.
   * Targets are cached temporarily, and can be used to inject failures until the cache expires.
   *
   * @return a random target and a token to be used for injecting failures on that target
   */
  ComposableFuture<Map<String, Target>> selectTarget();

  /**
   * @return a mapping [injector_id -> description]
   */
  ComposableFuture<Map<String, String>> faultOptions();

  /**
   * Injects the fault specified by it's ID on the target specified by the given token
   * @param targetToken a token that must match a target token recently returned from the <code>/selectTarget</code> endpoint
   * @param faultId the id of the fault injector to be used
   * @return execution output
   */
  ComposableFuture<String> trigger(String targetToken, String faultId);

  /**
   * @return A mapping of the triggered fault since startup [trigger_id -> fault]
   */
  ComposableFuture<Map<String, Fault>> log();

  /**
   * Reverts a single triggered fault
   * @param faultId the id of the triggered fault. Must match an id from the audit log.
   * @return revert execution output
   */
  ComposableFuture<String> revert(String faultId);

  /**
   * Reverts all triggered faults
   * @return revert execution output
   */
  ComposableFuture<String> revertAll();
}
