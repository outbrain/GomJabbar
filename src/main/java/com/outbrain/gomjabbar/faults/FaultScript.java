package com.outbrain.gomjabbar.faults;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class FaultScript extends BaseFaultData {
  public final String scriptUrl;
  public final String scriptArgs;
  public final String revertScriptUrl;
  public final String revertScriptArgs;

  public FaultScript(final BaseFaultData baseFaultData, final String scriptUrl, final String scriptArgs, final String revertScriptUrl, final String revertScriptArgs) {
    super(baseFaultData);
    this.scriptUrl = Objects.requireNonNull(scriptUrl, "scriptUrl must not be null");
    this.scriptArgs = scriptArgs;
    this.revertScriptUrl = revertScriptUrl;
    this.revertScriptArgs = revertScriptArgs;
  }

}
