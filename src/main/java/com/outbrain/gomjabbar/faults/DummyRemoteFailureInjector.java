package com.outbrain.gomjabbar.faults;

import com.outbrain.gomjabbar.targets.Target;

import java.util.Optional;

/**
 * A dummy failure used for testing, simulations, and debugging - executes a harmless command on rundeck
 * @author Eran Harel
 */
public class DummyRemoteFailureInjector extends AbstractFaultCommandInjector {

  public DummyRemoteFailureInjector(final RundeckCommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public String description() {
    return "Runs a harmless shell command on remote targets - should take about 5 sec to complete";
  }

  @Override
  protected String formatCommand(final Target target) {
    return "for i in `seq 1 5`; do echo $i; sleep 1; done\n";
  }

  @Override
  protected Optional<String> formatRevertCommand(final Target target) {
    return Optional.empty();
  }
}
