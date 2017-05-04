package com.outbrain.gomjabbar.targets;

import com.outbrain.gomjabbar.config.ConfigParser;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.consul.ConsulAPI;
import com.outbrain.ob1k.consul.ConsulCatalog;
import com.outbrain.ob1k.consul.ConsulHealth;
import com.outbrain.ob1k.consul.HealthInfoInstance;
import com.outbrain.ob1k.consul.TagsUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class ConsulTargetsCache implements TargetsCollector {

  private static final Logger log = LoggerFactory.getLogger(ConsulTargetsCache.class);

  private static final int BATCH_SIZE = 20;

  private final ConsulHealth health;
  private final ConsulCatalog catalog;
  private final TargetFilters targetFilters;
  private final Reloader reloader = new Reloader();

  private volatile Map<String, Map<String, List<HealthInfoInstance>>> cache = null;

  public ConsulTargetsCache(final ConsulHealth health, final ConsulCatalog catalog, final TargetFilters targetFilters) {
    this.catalog = catalog;
    this.health = Objects.requireNonNull(health, "health must not be null");
    this.targetFilters = Objects.requireNonNull(targetFilters, "targetFilters must not be null");

    reloader.start();
  }

  @Override
  public ComposableFuture<Target> chooseTarget() {
    return reloader.reloadFuture.map(__ ->  {
      final String dc = chooseDC();
      final String module = chooseModule(dc);

      return chooseTarget(dc, module);
    });
  }

  private String chooseDC() {
    return randomElement(cache.keySet());
  }

  private String chooseModule(final String dc) {
    return randomElement(cache.get(dc).keySet());
  }

  private Target chooseTarget(final String dc, final String module) {
    final List<HealthInfoInstance> instances = cache.get(dc).get(module);
    final HealthInfoInstance randomInstance = randomElement(instances);

    return new Target(randomInstance.Node.Node, randomInstance.Service.Service, extractServicetype(randomInstance), instances.size(), randomInstance.Service.Tags);
  }

  private String extractServicetype(final HealthInfoInstance instance) {
    final String servicetype = TagsUtil.extractTag(instance.Service.Tags, "servicetype");
    return servicetype == null ? UNDEFINED : servicetype;
  }


  private <T> T randomElement(final Collection<T> collection) {
    return collection.stream()
      .skip(ThreadLocalRandom.current().nextInt(collection.size()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Unexpected missing elemenet o_0"));
  }

  private ComposableFuture<?> reloadAsync() {
    final ComposableFuture<Map<String, Map<String, List<HealthInfoInstance>>>> cacheFuture = catalog.datacenters()
      .map(this::fetchDcServiceMappingAsync)
      .flatMap(dc2serviceInstancesFutures -> ComposableFutures.all(dc2serviceInstancesFutures.values())
        .map(__ -> transformInstanceFuturesMap(dc2serviceInstancesFutures)));
    cacheFuture.consume(newCacheTry -> {
      if (newCacheTry.isFailure()) {
        log.error("Failed to fetch targets", newCacheTry.getError());
      }
      cache = newCacheTry.getOrElse(HashMap::new);
    });

    return cacheFuture;
  }

  private Map<String, ComposableFuture<Map<String, List<HealthInfoInstance>>>> fetchDcServiceMappingAsync(final Collection<String> dcs) {
    log.debug("consul dcs: {}", dcs);
    return dcs.stream().filter(targetFilters.dcFilter())
      .collect(Collectors.toMap(Function.identity(), this::service2instances));
  }

  private Map<String, Map<String, List<HealthInfoInstance>>> transformInstanceFuturesMap(final Map<String, ComposableFuture<Map<String, List<HealthInfoInstance>>>> dc2serviceInstancesFutures) {
    final Map<String, Map<String, List<HealthInfoInstance>>> dc2serviceInstances = dc2serviceInstancesFutures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
      try {
        return e.getValue().recover(t -> {
          log.error("Failed to load targets for DC=" + e.getKey(), t);
          return new HashMap<>();
        }).get();
      } catch (InterruptedException | ExecutionException ex) {
        throw new RuntimeException("shouldn't happen as we're in the future map callback handler", ex);
      }
    }));

    dc2serviceInstances.entrySet().removeIf(e -> MapUtils.isEmpty(e.getValue()));
    return  dc2serviceInstances;
  }

  private ComposableFuture<Map<String, List<HealthInfoInstance>>> service2instances(final String dc) {
    log.debug("Fetching services for {} dc ", dc);
    return catalog.services(dc)
      .flatMap(service2tags -> {
        final List<String> filteredServices = service2tags.keySet()
          .stream()
          .filter(targetFilters.moduleFilter())
          .collect(Collectors.toList());

        return ComposableFutures.batch(filteredServices, BATCH_SIZE, service -> fetchServiceInstancesAsync(dc, service))
          .map(pairs ->
            pairs.stream()
              .filter(p -> !CollectionUtils.isEmpty(p.getRight()))
              .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
      });
  }

  private ComposableFuture<Pair<String, List<HealthInfoInstance>>> fetchServiceInstancesAsync(final String dc, final String service) {
    return health.fetchInstancesHealth(service, dc)
      .map(instances -> Pair.of(service, filterInstances(instances)));
  }

  private List<HealthInfoInstance> filterInstances(final Collection<HealthInfoInstance> instances) {
    return instances.stream().filter(targetFilters.instanceFilter()).collect(Collectors.toList());
  }

  // debug...
  private void print() {
    reloader.reloadFuture.consume(__ -> cache.entrySet().forEach(dc2services -> {
      System.out.println(dc2services.getKey());
      dc2services.getValue().entrySet().forEach(service2instances -> {
        System.out.println("\t" + service2instances.getKey());
        service2instances.getValue().forEach(i -> System.out.println("\t\t" + i.Service.Tags));
      });
    }));
  }

  public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
    final URL configFileUrl = new URL("file:./config-template.yaml");
    final TargetFilters targetFilters = ConfigParser.parseConfiguration(configFileUrl).targetFilters;

    final ConsulTargetsCache consulTargetsCache = new ConsulTargetsCache(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), targetFilters);

//    consulTargetsCache.print();

    for (int i = 0; i < 1000; i++) {
      System.out.println(consulTargetsCache.chooseTarget().get());
      Thread.sleep(2000);
    }
  }

  private class Reloader {

    private static final int RELOAD_DELAY_MINUTES = 5;
    private final AtomicBoolean isReloading = new AtomicBoolean(false);

    private volatile ComposableFuture<?> reloadFuture;

    private void start() {
      reload();
    }

    private boolean reload() {
      if (isReloading.compareAndSet(false, true)) {
        log.info("Reloading cache...");
        final ComposableFuture<?> nextReloadFuture = reloadAsync();
        if(null == reloadFuture) {
          reloadFuture = nextReloadFuture;
        }

        nextReloadFuture.consume(result -> {
          reloadFuture = nextReloadFuture;
          if(isReloading.compareAndSet(true, false)) {
            log.info("Scheduling the next cache reload in {} minutes", RELOAD_DELAY_MINUTES);
            ComposableFutures.schedule(this::reload, RELOAD_DELAY_MINUTES, TimeUnit.MINUTES);
          }
        });

        return true;
      }

      log.debug("Cache reloading is already in progress... skipping...");
      return false;
    }
  }
}
