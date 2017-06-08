package com.outbrain.gomjabbar.config;

import com.outbrain.ob1k.consul.HealthInfoInstance;
import org.junit.Test;

import java.net.URL;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the {@link ConfigParser} implementation
 * @author Eran Harel
 */
public class ConfigParserTest {

  @Test(expected = NullPointerException.class)
  public void testParseConfiguration_nullUrl() throws Exception {
    ConfigParser.parseConfiguration(null);
  }

  @Test(expected = NullPointerException.class)
  public void testParseConfiguration_emptyConfiguration() throws Exception {
    final URL configFileUrl = getClass().getClassLoader().getResource("test-config-empty.yaml");
    ConfigParser.parseConfiguration(configFileUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseConfiguration_invalidExecutorClass() throws Exception {
    final URL configFileUrl = getClass().getClassLoader().getResource("test-config-bogus-execution-class.yaml");
    ConfigParser.parseConfiguration(configFileUrl);
  }

  @Test
  public void testParseConfiguration_executionOnly() throws Exception {
    final URL configFileUrl = getClass().getClassLoader().getResource("test-config-only-execution.yaml");
    final Configuration configuration = ConfigParser.parseConfiguration(configFileUrl);

    // empty filters mean everything discoverable
    assertTrue("dcFilter", configuration.targetFilters.dcFilter().test("dc1"));
    assertTrue("moduleFilter", configuration.targetFilters.moduleFilter().test("mod1"));
    assertTrue("instanceFilter", configuration.targetFilters.instanceFilter().test(createHealthInfoInstance()));


    assertTrue("commands should be empty", configuration.commands.isEmpty());
    assertTrue("scripts should be empty", configuration.scripts.isEmpty());
  }

  @Test
  public void testParseConfiguration_fullParse() throws Exception {
    final URL configFileUrl = getClass().getClassLoader().getResource("test-config.yaml");
    final Configuration configuration = ConfigParser.parseConfiguration(configFileUrl);

    assertTrue("dcFilter should pass for included dc", configuration.targetFilters.dcFilter().test("included_dc1"));
    assertFalse("dcFilter should fail for non included dc", configuration.targetFilters.dcFilter().test("666"));
    assertFalse("dcFilter should fail for excluded dc", configuration.targetFilters.dcFilter().test("excluded_dc1"));
    assertTrue("moduleFilter should pass for included module", configuration.targetFilters.moduleFilter().test("safe_module2"));
    assertFalse("moduleFilter should fail for non included module", configuration.targetFilters.moduleFilter().test("999"));
    assertFalse("moduleFilter should fail for excluded module", configuration.targetFilters.moduleFilter().test("unsafe_module1"));
    final HealthInfoInstance healthInfoInstance = createHealthInfoInstance();
    assertFalse("instanceFilter should fail for no matching tags", configuration.targetFilters.instanceFilter().test(healthInfoInstance));
    healthInfoInstance.Service.Tags.add("inc");
    assertTrue("instanceFilter should pass for included tags", configuration.targetFilters.instanceFilter().test(healthInfoInstance));
    healthInfoInstance.Service.Tags.add("ex");
    assertFalse("instanceFilter shoul fail for excluded tags", configuration.targetFilters.instanceFilter().test(healthInfoInstance));


    assertFalse("missing commands", configuration.commands.isEmpty());
    assertFalse("missing scripts", configuration.scripts.isEmpty());
  }


  private HealthInfoInstance createHealthInfoInstance() {
    final HealthInfoInstance.Node node = new HealthInfoInstance.Node();
    node.Node = "myservice.node";
    node.Address = "1.1.1.1";

    final HealthInfoInstance.Service service = new HealthInfoInstance.Service();
    service.Tags = newHashSet("httpPort-8080", "contextPath-/");
    service.Address = "5.5.5.5";

    final HealthInfoInstance instance = new HealthInfoInstance();

    instance.Node = node;
    instance.Service = service;
    instance.Checks = emptyList();

    return instance;
  }
}