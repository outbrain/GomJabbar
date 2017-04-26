package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;

import java.util.Optional;

/**
 * Kills services gracefully using init.d service stop
 * @author Eran Harel
 */
public class InitdStopper extends AbstractCommandFaultInjector {

  public InitdStopper(final RundeckCommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public String description() {
    return "Gracefully shuts down services";
  }

  @Override
  protected String formatCommand(final Target target) {
    return String.format("sudo service %s stop", target.getModule());
  }

  @Override
  protected Optional<String> formatRevertCommand(final Target target) {
    return Optional.of(String.format("sudo service %s start", target.getModule()));
  }

}
