package com.outbrain.gomjabbar.faults;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class FaultCommand extends BaseFaultData {

  public final String command;
  public final String revertCommand;

  public FaultCommand(final BaseFaultData baseFaultData, final String command, final String revertCommand) {
    super(baseFaultData);
    this.command = Objects.requireNonNull(command, "command must not be null");
    this.revertCommand = revertCommand;
  }
}
