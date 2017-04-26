package com.outbrain.gomjabbar.faults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class RundeckCommand {

  private final String project;
  private final Collection<String> hosts;
  private final String exec;
  private final String url;
  private final String argString;

  // I need to make some marshalers happy ;)
  RundeckCommand() {
    this(null, null, null, null, null);
  }

  private RundeckCommand(final String project, final Collection<String> hosts, final String command, final String url, final String argString) {
    this.project = project;
    this.hosts = new ArrayList<>(hosts);
    this.exec = command;
    this.url = url;
    this.argString = argString;
  }

  public String getExec() {
    return exec;
  }

  public String getFilter() {
    return "hostname: " + hosts.stream().collect(Collectors.joining(","));
  }

  public String getProject() {
    return project;
  }

  public String getArgString() {
    return argString;
  }

  public String getUrl() {
    return url;
  }

  public static class Builder {
    private String project = "ops";
    private final Collection<String> hosts;

    private Builder(final Collection<String> hosts) {
      this.hosts = hosts;
    }

    public static Builder forTarget(final String host) {
      return forTarget(Collections.singleton(host));
    }

    public static Builder forTarget(final Collection<String> hosts) {
      return new Builder(hosts);
    }

    public Builder project(final String project) {
      this.project = project;
      return this;
    }

    public RundeckCommand buildCommand(final String command) {
      return new RundeckCommand(project, hosts, command, null, null);
    }

    public RundeckCommand buildScriptUrl(final String scriptUrl, final String scriptArgs) {
      return new RundeckCommand(project, hosts, null, scriptUrl, scriptArgs);
    }
  }
}
