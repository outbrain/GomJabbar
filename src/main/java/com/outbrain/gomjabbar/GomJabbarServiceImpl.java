package com.outbrain.gomjabbar;

import com.outbrain.ob1k.cache.LocalAsyncCache;
import com.outbrain.ob1k.cache.TypedCache;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Eran Harel
 */
public class GomJabbarServiceImpl implements GomJabbarService {

  private final FaultInjectors faultInjectors;
  private final TargetsCollector targetsCollector;
  private final AuditLog auditLog;

  private final TypedCache<String, Target> targetsCache = new LocalAsyncCache<>(1000, 5, TimeUnit.MINUTES, null, "targets");

  GomJabbarServiceImpl(final FaultInjectors faultInjectors, final TargetsCollector targetsCollector, final AuditLog auditLog) {
    this.faultInjectors = Objects.requireNonNull(faultInjectors, "faultInjectors must not be null");
    this.targetsCollector = Objects.requireNonNull(targetsCollector, "targetsCollector must not be null");
    this.auditLog = Objects.requireNonNull(auditLog, "auditLog must not be null");
  }

  @Override
  public ComposableFuture<Map<String, Target>> selectTarget() {
    return targetsCollector.chooseTarget()
      .flatMap(target -> {
        final String token = String.valueOf(ThreadLocalRandom.current().nextLong());
        return targetsCache.setAsync(token, target).map(__ -> Collections.singletonMap(token, target));
      });
  }

  @Override
  public ComposableFuture<Map<String, String>> faultOptions() {
    return ComposableFutures.fromValue(faultInjectors.options());
  }

  @Override
  public ComposableFuture<String> trigger(final String targetToken, final String faultId) {
    return targetsCache.getAsync(targetToken)
      .flatMap(target ->
        target == null ?
          ComposableFutures.fromValue("Token " + targetToken + " has expired") :
          faultInjectors.getFaultInjector(faultId)
            .injectFailure(target)
            .andThen(__ -> auditLog.log(new Fault(target, faultId))));
  }
}
