package com.outbrain.gomjabbar.faults;

/**
 * @author Eran Harel
 */
public class RundeckCommand {

  private final String project = "ops";
  private final String host;
  private final String exec;

  // I need to make some marshalers happy ;)
  RundeckCommand() {
    this(null, null);
  }

  public RundeckCommand(final String host, final String command) {
    this.host = host;
    this.exec = command;
  }

  public String getExec() {
    return exec;
  }

  public String getFilter() {
    return "hostname: " + host;
  }

  public String getProject() {
    return project;
  }

}
