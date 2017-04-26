package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;

import java.util.Optional;

/**
 * Introduces high latency, packet loss, and decreased BW
 * @author Eran Harel
 */
public class TraficController extends AbstractCommandFaultInjector {

  private static final String TC_CMD = "DEV=`sudo route | grep default | awk \"'{print $NF}'\"`; sudo tc qdisc add dev $DEV root netem delay %dms loss %d.00%%; sudo tc -s qdisc";
  private static final String TC_REVERT_CMD = "sudo tc qdisc del dev `route | grep default | awk \"'{print $NF}'`\" root; sudo rmmod sch_netem; sudo tc -s qdisc";

  private final int packetLossPercent;
  private final int latencyMS;

  public TraficController(final RundeckCommandExecutor commandExecutor, final int packetLossPercent, final int latencyMS) {
    super(commandExecutor);
    this.packetLossPercent = packetLossPercent;
    this.latencyMS = latencyMS;
  }

  @Override
  public String description() {
    return "Introduces high latency, packet loss, and decreased BW";
  }

  @Override
  protected String formatCommand(final Target target) {
    return String.format(TC_CMD, latencyMS, packetLossPercent);
  }

  @Override
  protected Optional<String> formatRevertCommand(final Target target) {
    return Optional.of(TC_REVERT_CMD);
  }
}
