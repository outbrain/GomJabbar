package com.outbrain.gomjabbar.testutil;

import com.outbrain.ob1k.consul.HealthInfoInstance;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;

/**
 * @author Eran Harel
 */
public class HealthInfoInstanceHealper {

  private static final AtomicInteger NODE_ID = new AtomicInteger();

  public static HealthInfoInstance createMockHealthInfoInstance(String MyService) {
    final HealthInfoInstance.Node node = new HealthInfoInstance.Node();
    final int ID = NODE_ID.getAndIncrement();
    node.Node = "myservice.node" + ID;
    node.Address = "1.1.1." + ID;

    final HealthInfoInstance.Service service = new HealthInfoInstance.Service();
    service.Tags = newHashSet("httpPort-8080", "contextPath-/");
    service.Address = "5.5.5." + ID;
    service.Service = MyService;
    service.ID = MyService + ID;

    final HealthInfoInstance instance = new HealthInfoInstance();

    instance.Node = node;
    instance.Service = service;
    instance.Checks = emptyList();

    return instance;
  }
}
