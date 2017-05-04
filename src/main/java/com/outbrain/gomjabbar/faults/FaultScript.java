package com.outbrain.gomjabbar.faults;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class FaultScript {
  public final String id;
  public final String description;
  public final String scriptUrl;
  public final String scriptArgs;
  public final String revertScriptUrl;
  public final String revertScriptArgs;

  public FaultScript(final String id, final String description, final String scriptUrl, final String scriptArgs, final String revertScriptUrl, final String revertScriptArgs) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.description = description;
    this.scriptUrl = Objects.requireNonNull(scriptUrl, "scriptUrl must not be null");
    this.scriptArgs = scriptArgs;
    this.revertScriptUrl = revertScriptUrl;
    this.revertScriptArgs = revertScriptArgs;
  }

}
