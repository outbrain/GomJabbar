package com.outbrain.gomjabbar.execution;

import com.google.common.io.CharStreams;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.outbrain.ob1k.concurrent.ComposableFutures.submit;

/**
 * An ansible based implementation for remote command executions.
 * @author Eran Harel
 */
public class AnsibleCommandExecutor implements CommandExecutor {

  private static final Logger log = LoggerFactory.getLogger(AnsibleCommandExecutor.class);
  private static final String ANSIBLE = "ansible";
  private static final String FAULT_COMMAND_PATH = "/tmp/fault-command";

  public static CommandExecutor createCommandExecutor() {
    return new AnsibleCommandExecutor();
  }

  @Override
  public ComposableFuture<String> executeCommandAsync(RemoteCommand command) {
    return execProcess(createCommand(command));
  }

  private ComposableFuture<String> execProcess(final List<String> command) {
    return submit(true, () -> {
      final Process process = new ProcessBuilder(command)
        .start();
      process.waitFor(20, TimeUnit.SECONDS);

      return CharStreams.toString(new InputStreamReader(process.getInputStream()));
    });
  }

  private List<String> createCommand(RemoteCommand command) {
    final List<String> cmd = Arrays.asList(ANSIBLE, "-m", "raw", hostPattern(command), "-a", command.getExec());

    log.debug("ansible command: [{}]", cmd.stream().collect(Collectors.joining(" ")));
    return cmd;
  }

  private String hostPattern(RemoteCommand command) {
    return command.hosts().stream().collect(Collectors.joining(";"));
  }

  @Override
  public ComposableFuture<String> executeScriptByUrlAsync(RemoteCommand command) {
    final String hostPattern = hostPattern(command);
    return copyScriptToRemoteTargets(hostPattern, command.getUrl())
      .flatMap(getUrlOut -> {
        log.debug("getUrlOut={}", getUrlOut);
        return execProcess(Arrays.asList(ANSIBLE, "-m", "raw", hostPattern, "-a", FAULT_COMMAND_PATH+ " " + command.getArgString()));
      });
  }

  private ComposableFuture<String> copyScriptToRemoteTargets(final String hostPattern, final String commandUrl) {
    final String getUrlParams = String .format("url=%s dest=%s mode=0744 validate_certs=no", commandUrl, FAULT_COMMAND_PATH);
    return execProcess(Arrays.asList(ANSIBLE, hostPattern, "-m", "get_url", "-a", getUrlParams));
  }

}
