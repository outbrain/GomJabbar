package com.outbrain.gomjabbar;

import com.google.common.base.Joiner;
import com.outbrain.gomjabbar.audit.AuditLog;
import com.outbrain.gomjabbar.faults.Fault;
import com.outbrain.gomjabbar.faults.FaultInjectors;
import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.gomjabbar.targets.TargetsCollector;
import com.outbrain.ob1k.cache.LocalAsyncCache;
import com.outbrain.ob1k.cache.TypedCache;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class GomJabbarServiceImpl implements GomJabbarService {

  private final FaultInjectors faultInjectors;
  private final TargetsCollector targetsCollector;
  private final AuditLog auditLog;

  private final TypedCache<String, Target> targetsCache = new LocalAsyncCache<>(10000, 2, TimeUnit.HOURS, null, "targets");

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

  @Override
  public ComposableFuture<Map<String, Fault>> log() {
    return ComposableFutures.submit(true, auditLog::list);
  }

  @Override
  public ComposableFuture<String> revert(final String faultId) {
    return auditLog.findFault(faultId)
      .map(this::revertFault)
      .orElse(ComposableFutures.fromValue("No such fault " + faultId));
  }

  private ComposableFuture<String> revertFault(final Fault fault) {
    return faultInjectors.getFaultInjector(fault.faultInjectorId).revertFailure(fault.target);
  }

  @Override
  public ComposableFuture<String> revertAll() {
    final List<ComposableFuture<String>> futureReverts = auditLog.list().values().stream().map(this::revertFault).collect(Collectors.toList());
    return ComposableFutures.all(futureReverts).map(strings -> Joiner.on('\n').join(strings));
  }
}
