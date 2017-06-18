package com.outbrain.gomjabbar.targets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.outbrain.gomjabbar.testutil.HealthInfoInstanceHealper;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.consul.ConsulCatalog;
import com.outbrain.ob1k.consul.ConsulHealth;
import com.outbrain.ob1k.consul.HealthInfoInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Test cases for the {@link ConsulTargetsCache} implementation
 * @author Eran Harel
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsulTargetsCacheTest {

  private static final String INCLUDED_DC = "dc1";
  private static final String EXCLUDED_DC = "dc2";
  private static final String EXCLUDED_SERVICE = "s1";
  private static final String INCLUDED_MODULE = "s2";
  private static final HealthInfoInstance INCLUDED_INSTANCE = HealthInfoInstanceHealper.createMockHealthInfoInstance(INCLUDED_MODULE);
  private static final HealthInfoInstance EXCLUDED_INSTANCE = HealthInfoInstanceHealper.createMockHealthInfoInstance(INCLUDED_MODULE);

  @Mock
  private ConsulHealth health;
  @Mock
  private ConsulCatalog catalog;
  @Mock
  private TargetFilters targetFilters;

  @Test
  public void testChooseTarget_emptyCache_shouldThrowException() throws Exception {
    when(catalog.datacenters()).thenReturn(ComposableFutures.fromValue(Collections.emptySet()));
    when(targetFilters.dcFilter()).thenReturn(__ -> true);
    final ConsulTargetsCache targetsProvider = new ConsulTargetsCache(health, catalog, targetFilters);
    try {
      targetsProvider.chooseTarget().get();
      fail("expected to fail on empty cache");
    } catch (final ExecutionException e) {
      assertEquals(IllegalStateException.class, e.getCause().getClass());
    }
  }

  @Test
  public void testChooseTarget() throws Exception {
    when(catalog.datacenters()).thenReturn(ComposableFutures.fromValue(Sets.newHashSet(INCLUDED_DC, EXCLUDED_DC)));
    when(catalog.services(INCLUDED_DC)).thenReturn(createServicesMap());
    when(health.fetchInstancesHealth(INCLUDED_MODULE, INCLUDED_DC)).thenReturn(createInstances());
    when(targetFilters.dcFilter()).thenReturn(INCLUDED_DC::equals);
    when(targetFilters.moduleFilter()).thenReturn(INCLUDED_MODULE::equals);
    when(targetFilters.instanceFilter()).thenReturn(__ -> __ == INCLUDED_INSTANCE);

    final ConsulTargetsCache targetsProvider = new ConsulTargetsCache(health, catalog, targetFilters);
    final Target actualTarget = targetsProvider.chooseTarget().get();

    assertEquals("target host", INCLUDED_INSTANCE.Node.Node, actualTarget.getHost());
    assertEquals("target service", INCLUDED_INSTANCE.Service.Service, actualTarget.getModule());
    assertEquals("target tags", INCLUDED_INSTANCE.Service.Tags, actualTarget.getTags());
  }

  private ComposableFuture<List<HealthInfoInstance>> createInstances() {
    return ComposableFutures.fromValue(Lists.newArrayList(INCLUDED_INSTANCE));
  }

  private ComposableFuture<Map<String, Set<String>>> createServicesMap() {
    final Map<String, Set<String>> services = Maps.asMap(Sets.newHashSet(EXCLUDED_SERVICE, INCLUDED_MODULE), __ -> Collections.emptySet());
    return ComposableFutures.fromValue(services);
  }

}