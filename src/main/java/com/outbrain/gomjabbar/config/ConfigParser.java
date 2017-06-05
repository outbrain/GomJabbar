package com.outbrain.gomjabbar.config;

import com.outbrain.gomjabbar.faults.BaseFaultData;
import com.outbrain.gomjabbar.faults.FaultCommand;
import com.outbrain.gomjabbar.faults.FaultScript;
import com.outbrain.gomjabbar.targets.DefaultTargetsFilter;
import com.outbrain.gomjabbar.targets.TargetFilters;
import org.apache.commons.lang3.tuple.Pair;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses the config file. Duh.
 * @author Eran Harel
 */
public class ConfigParser {

  @SuppressWarnings("unchecked")
  public static Configuration parseConfiguration(final URL configFileUrl) {
    final Yaml yaml = new Yaml();
    try {
      @SuppressWarnings("unchecked")
      final Map<String, Map<String, ?>> config =
        (Map<String, Map<String, ?>>) yaml.load(configFileUrl.openStream());

      final TargetFilters targetFilters = parseFilters((Map<String, Map<String, List<String>>>) config.get("filters"));
      final Collection<FaultScript> scripts = parseScripts((Map<String, Map<String, Object>>) config.get("scripts"));
      final Collection<FaultCommand> commands = parseCommands((Map<String, Map<String, Object>>) config.get("commands"));

      final Class<?> commandExecutorFactoryClass = parseExecutionParams(config);

      return new Configuration(targetFilters, scripts, commands, commandExecutorFactoryClass);
    } catch (final IOException e) {
      throw new RuntimeException("failed to load config file from url: " + configFileUrl, e);
    }

  }

  private static Class<?> parseExecutionParams(Map<String, Map<String, ?>> config) {
    final String className = (String) config.get("execution").get("command_executor_factory");
    try {
      return Class.forName(className);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to load command executor factory class: " + className, e);
    }
  }

  private static Collection<FaultCommand> parseCommands(Map<String, Map<String, Object>> commands) {
    return null == commands ?
      Collections.emptyList() :
      commands.entrySet().stream().map(ConfigParser::parseCommand).collect(Collectors.toList());
  }

  private static FaultCommand parseCommand(Map.Entry<String, Map<String, Object>> commandData) {
    BaseFaultData baseFaultData = parseBaseFaultData(commandData);
    final String command = (String) commandData.getValue().get("fail");
    final String revertCommand = (String) commandData.getValue().get("revert");

    return new FaultCommand(baseFaultData, command, revertCommand);
  }

  private static BaseFaultData parseBaseFaultData(Map.Entry<String, Map<String, Object>> commandData) {
    final String id = commandData.getKey();
    final String description = (String) commandData.getValue().get("description");
    return new BaseFaultData(id, description);
  }

  private static Collection<FaultScript> parseScripts(Map<String, Map<String, Object>> scripts) {
    return null == scripts ?
      Collections.emptyList() :
      scripts.entrySet().stream().map(ConfigParser::parseScript).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private static FaultScript parseScript(final Map.Entry<String, Map<String, Object>> scriptData) {
    BaseFaultData baseFaultData = parseBaseFaultData(scriptData);
    final Map<String, String> failScript = (Map<String, String>) scriptData.getValue().get("fail");
    final Map<String, String> revertScript = (Map<String, String>) scriptData.getValue().get("revert");

    return new FaultScript(baseFaultData,
      failScript.get("URL"), failScript.get("args"),
      revertScript.get("URL"), revertScript.get("args"));
  }

  private static TargetFilters parseFilters(final Map<String, Map<String, List<String>>> filters) {
    final Pair<Set<String>, Set<String>> clusters = parseFilter(filters, "clusters");
    final Pair<Set<String>, Set<String>> modules = parseFilter(filters, "modules");
    final Pair<Set<String>, Set<String>> tags = parseFilter(filters, "tags");
    return new DefaultTargetsFilter(clusters.getLeft(), clusters.getRight(),
      modules.getLeft(), modules.getRight(),
      tags.getLeft(), tags.getRight());
  }

  private static Pair<Set<String>, Set<String>> parseFilter(final Map<String, Map<String, List<String>>> filters, final String scope) {
    final Map<String, List<String>> filter = Optional.ofNullable(filters.get(scope)).orElse(Collections.emptyMap());
    return Pair.of(parseFilterScope(filter, "include"), parseFilterScope(filter, "exclude"));
  }

  private static Set<String> parseFilterScope(final Map<String, List<String>> filter, final String scope) {
    return new HashSet<>(Optional.ofNullable(filter.get(scope)).orElse(Collections.emptyList()));
  }

}
