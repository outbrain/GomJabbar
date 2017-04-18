package com.outbrain.gomjabbar.faults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class RundeckCommand {

  private final String project = "ops";
  private final Collection<String> hosts;
  private final String exec;

  // I need to make some marshalers happy ;)
  RundeckCommand() {
    this((String) null, null);
  }

  public RundeckCommand(final String host, final String command) {
    this(Collections.singleton(host), command);
  }

  public RundeckCommand(final Collection<String> hosts, final String command) {
    this.hosts = new ArrayList<>(hosts);
    this.exec = command;
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

}
