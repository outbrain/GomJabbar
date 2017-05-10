package com.outbrain.gomjabbar.execution;

import java.util.List;

/**
 * @author Eran Harel
 */
public class RundeckExecutionStatus {
  private boolean completed;
  private List<Entry> entries;

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public List<Entry> getEntries() {
    return entries;
  }

  public void setEntries(List<Entry> entries) {
    this.entries = entries;
  }

  public static class Entry {

    private String log;

    public String getLog() {
      return log;
    }

    public void setLog(String log) {
      this.log = log;
    }
  }
}
