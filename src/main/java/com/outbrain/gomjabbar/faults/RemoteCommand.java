package com.outbrain.gomjabbar.faults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class RemoteCommand {

  private final String project;
  private final Collection<String> hosts;
  private final String exec;
  private final String url;
  private final String argString;

  // I need to make some marshalers happy ;)
  RemoteCommand() {
    this(null, null, null, null, null);
  }

  private RemoteCommand(final String project, final Collection<String> hosts, final String command, final String url, final String argString) {
    this.project = project;
    this.hosts = new ArrayList<>(hosts);
    this.exec = command;
    this.url = url;
    this.argString = argString;
  }

  public String getExec() {
    return exec;
  }

  public String getRundeckFilter() {
    return "hostname: " + hosts.stream().collect(Collectors.joining(","));
  }

  public String getAnsibleFilter() {
    return hosts.stream().collect(Collectors.joining(";"));
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

    static Builder forTarget(final String host) {
      return forTarget(Collections.singleton(host));
    }

    static Builder forTarget(final Collection<String> hosts) {
      return new Builder(hosts);
    }

    public Builder project(final String project) {
      this.project = project;
      return this;
    }

    RemoteCommand buildCommand(final String command) {
      return new RemoteCommand(project, hosts, command, null, null);
    }

    RemoteCommand buildScriptUrl(final String scriptUrl, final String scriptArgs) {
      return new RemoteCommand(project, hosts, null, scriptUrl, scriptArgs);
    }
  }
}
