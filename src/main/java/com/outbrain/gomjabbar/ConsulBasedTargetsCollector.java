package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.handlers.FutureSuccessHandler;
import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.consul.ConsulCatalog;
import com.outbrain.ob1k.consul.ConsulHealth;
import com.outbrain.ob1k.consul.HealthInfoInstance;
import com.outbrain.ob1k.consul.TagsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class ConsulBasedTargetsCollector implements TargetsCollector {

  private static final Logger log = LoggerFactory.getLogger(ConsulBasedTargetsCollector.class);

  private final Random random = new Random();
  private final ConsulHealth health;
  private final ConsulCatalog catalog;

  public ConsulBasedTargetsCollector(final ConsulHealth health, final ConsulCatalog catalog) {
    this.catalog = catalog;
    this.health = Objects.requireNonNull(health, "health must not be null");
  }

  public ComposableFuture<Target> chooseTarget() {
    final ComposableFuture<Target> targetFuture = chooseDC()
      .continueOnSuccess((FutureSuccessHandler<String, Target>) dc -> chooseModule(dc)
        .continueOnSuccess((FutureSuccessHandler<String, Target>) module -> chooseTarget(dc, module))
    );

    targetFuture.consume(targetTry -> {
      if(!targetTry.isSuccess()) {
        log.error("failed to fetch target", targetTry.getError());
      }
    });

    return targetFuture;
  }

  private ComposableFuture<String> chooseDC() {
    return catalog.datacenters().transform(dcs -> randomElement(dcs, undefined()));
  }

  private ComposableFuture<String> chooseModule(final String dc) {
    return catalog.services(dc).transform(service2tags -> randomElement(service2tags.keySet(), undefined()));
  }

  private Function<Void, String> undefined() {
    return e -> UNDEFINED;
  }

  private ComposableFuture<Target> chooseTarget(final String dc, final String module) {
    return health.fetchInstancesHealth(module, dc)
      .transform(instances ->
        randomElement(
          instances.stream()
            .filter(instance -> instance.Checks.stream().allMatch(check -> "passing".equals(check.Status)))
            .map(instance -> new Target(instance.Node.Node, instance.Service.Service, extractServicetype(instance), instances.size(), instance.Service.Tags))
            .collect(Collectors.toList()), e -> new Target(UNDEFINED, module, UNDEFINED, 0, new HashSet<String>())));
  }

  private String extractServicetype(final HealthInfoInstance instance) {
    final String servicetype = TagsUtil.extractTag(instance.Service.Tags, "servicetype");
    return servicetype == null ? UNDEFINED : servicetype;
  }

  private <T> T randomElement(final Collection<T> elements, final Function<Void, T> nullElm) {
    if (elements.isEmpty()) {
      return nullElm.apply(null);
    }

    final int extractIndex = random.nextInt(elements.size());
    int i = 0;
    for (final T elm : elements) {
      if(extractIndex == i) {
        return elm;
      }
      i++;
    }
    throw new IllegalStateException("what?");
  }

  public static void main(final String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    // run with -Dcom.outbrain.ob1k.consul.agent.address=my.consul.host:8500
    final TargetsCollector targetsCollector = new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog());
    for (int i = 0; i < 100; i++) {
      System.out.println(targetsCollector.chooseTarget().get(10, TimeUnit.SECONDS));
    }
  }
}
