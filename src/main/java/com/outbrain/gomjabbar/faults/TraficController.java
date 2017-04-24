package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;
import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.Objects;

/**
 * Introduces high latency, packet loss, and decreased BW
 * @author Eran Harel
 */
public class TraficController implements FaultInjector {

  private static final String TC_CMD = "DEV=`sudo route | grep default | awk \"'{print $NF}'\"`; sudo tc qdisc add dev $DEV root netem delay %dms loss %d.00%%; sudo tc -s qdisc";
  private static final String TC_REVERT_CMD = "sudo tc qdisc del dev `route | grep default | awk \"'{print $NF}'`\" root; sudo rmmod sch_netem; sudo tc -s qdisc";

  private final RundeckCommandExecutor commandExecutor;
  private final int packetLossPercent;
  private final int latencyMS;

  public TraficController(final RundeckCommandExecutor commandExecutor, final int packetLossPercent, final int latencyMS) {
    this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor must not be null");
    this.packetLossPercent = packetLossPercent;
    this.latencyMS = latencyMS;
  }

  @Override
  public String id() {
    return getClass().getName();
  }

  @Override
  public String description() {
    return "Introduces high latency, packet loss, and decreased BW";
  }

  @Override
  public ComposableFuture<String> injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), String.format(TC_CMD, latencyMS, packetLossPercent));
    return commandExecutor.executeCommandAsync(command);
  }

  @Override
  public ComposableFuture<String> revertFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), TC_REVERT_CMD);
    return commandExecutor.executeCommandAsync(command);
  }
}
