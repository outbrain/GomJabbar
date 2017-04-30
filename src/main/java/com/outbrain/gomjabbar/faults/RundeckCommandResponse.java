package com.outbrain.gomjabbar.faults;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Eran Harel
 */
public class RundeckCommandResponse {
  private String message;
  private Execution execution;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Execution getExecution() {
    return execution;
  }

  public void setExecution(Execution execution) {
    this.execution = execution;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
  }

  public static class Execution {
    private long id;
    private String href;
    private String permalink;

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getHref() {
      return href;
    }

    public void setHref(String href) {
      this.href = href;
    }

    public String getPermalink() {
      return permalink;
    }

    public void setPermalink(String permalink) {
      this.permalink = permalink;
    }

    @Override
    public String toString() {
      return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
  }
}
