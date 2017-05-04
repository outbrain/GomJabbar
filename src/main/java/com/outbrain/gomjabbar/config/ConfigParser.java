package com.outbrain.gomjabbar.config;

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
 * Parses the config file, (currently just the filters are extracted).
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
      final Collection<FaultScript> scripts = parseScripts((Map<String, Map<String, ?>>) config.get("scripts"));

      return new Configuration(targetFilters, scripts);
    } catch (final IOException e) {
      throw new RuntimeException("failed to load config file from url: " + configFileUrl, e);
    }

  }

  private static Collection<FaultScript> parseScripts(Map<String, Map<String, ?>> scripts) {
    return null == scripts ?
      Collections.emptyList() :
      scripts.entrySet().stream().map(ConfigParser::parseScript).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private static FaultScript parseScript(final Map.Entry<String, Map<String, ?>> scriptData) {
    final String id = scriptData.getKey();
    final String description = (String) scriptData.getValue().get("description");
    final Map<String, String> failScript = (Map<String, String>) scriptData.getValue().get("fail");
    final Map<String, String> revertScript = (Map<String, String>) scriptData.getValue().get("revert");

    return new FaultScript(id, description,
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
