package com.outbrain.gomjabbar.targets;

import com.outbrain.gomjabbar.config.ConfigParser;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.consul.ConsulCatalog;
import com.outbrain.ob1k.consul.ConsulHealth;
import com.outbrain.ob1k.consul.HealthInfoInstance;
import com.outbrain.ob1k.consul.TagsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class ConsulBasedTargetsCollector implements TargetsCollector {

  private static final Logger log = LoggerFactory.getLogger(ConsulBasedTargetsCollector.class);

  private final ConsulHealth health;
  private final ConsulCatalog catalog;
  private final TargetFilters targetFilters;

  public ConsulBasedTargetsCollector(final ConsulHealth health, final ConsulCatalog catalog, final TargetFilters targetFilters) {
    this.catalog = catalog;
    this.health = Objects.requireNonNull(health, "health must not be null");
    this.targetFilters = Objects.requireNonNull(targetFilters, "targetFilters must not be null");
  }

  public ComposableFuture<Target> chooseTarget() {
    final ComposableFuture<Target> targetFuture = chooseDC()
      .flatMap(dc -> chooseModule(dc)
        .flatMap(module -> chooseTarget(dc, module))
    );

    return targetFuture.recoverWith(e -> {
      log.error("failed to fetch target: {}", e.getMessage());
      return chooseTarget();
    });
  }

  private ComposableFuture<String> chooseDC() {
    return catalog.datacenters().flatMap(dcs -> randomElement(dcs, targetFilters.dcFilter(), "dc"));
  }

  private <T> ComposableFuture<T> fail(final String step) {
    return ComposableFutures.fromError(new RuntimeException("failed to select " + step));
  }

  private ComposableFuture<String> chooseModule(final String dc) {
    return catalog.services(dc)
      .flatMap(service2tags -> randomElement(service2tags.keySet(), targetFilters.moduleFilter(), "module"));
  }

  private ComposableFuture<Target> chooseTarget(final String dc, final String module) {
    return health.fetchInstancesHealth(module, dc)
      .flatMap(instances ->
        randomElement(instances, targetFilters.instanceFilter(), "instance").map(i -> new Target(i.Node.Node, i.Service.Service, extractServicetype(i), instances.size(), i.Service.Tags)));
  }

  private String extractServicetype(final HealthInfoInstance instance) {
    final String servicetype = TagsUtil.extractTag(instance.Service.Tags, "servicetype");
    return servicetype == null ? UNDEFINED : servicetype;
  }

  private <T> ComposableFuture<T> randomElement(final Collection<T> elements, final Predicate<T> filter, final String step) {
    final List<T> filteredElements = elements.stream().filter(filter).collect(Collectors.toList());
    if (filteredElements.isEmpty()) {
      return fail(step);
    }

    final int tries = filteredElements.size() * 20;
    for (int i = 0; i < tries; i++) {
      final T element = filteredElements.get(ThreadLocalRandom.current().nextInt(filteredElements.size()));
      if(element != null) {
        return ComposableFutures.fromValue(element);
      }
    }

    return fail(step);
  }

  public static void main(final String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    // run with -Dcom.outbrain.ob1k.consul.agent.address=my.consul.host:8500

    final TargetFilters targetFilters = ConfigParser.parseConfiguration(ConfigParser.class.getClassLoader().getResource("config.yaml"));
    final TargetsCollector targetsCollector = new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), targetFilters);

    for (int i = 0; i < 100; i++) {
      System.out.println(targetsCollector.chooseTarget().get(10, TimeUnit.SECONDS));
    }
  }
}
