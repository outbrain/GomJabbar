package com.outbrain.gomjabbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

  public static void main(String[] args) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    final String s = objectMapper.writeValueAsString(new RundeckCommand("host", "cmd"));
    System.out.println(s);
  }
}
