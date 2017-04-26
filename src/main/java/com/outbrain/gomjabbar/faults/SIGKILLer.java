package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;

import java.util.Optional;

/**
 * Kills services ungracefully (kill -9)
 * @author Eran Harel
 */
public class SIGKILLer extends AbstractCommandFaultInjector {

  public SIGKILLer(final RundeckCommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public String description() {
    return "Brutally kills service instances";
  }

  @Override
  protected String formatCommand(final Target target) {
    return String.format("sudo pkill -9 -f %s", target.getModule());
  }

  @Override
  protected Optional<String> formatRevertCommand(final Target target) {
    return Optional.of(String.format("sudo service %s start", target.getModule()));
  }

}
