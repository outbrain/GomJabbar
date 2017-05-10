package com.outbrain.gomjabbar.execution;

import com.google.common.io.CharStreams;
import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.outbrain.ob1k.concurrent.ComposableFutures.submit;

/**
 * @author Eran Harel
 */
public class AnsibleCommandExecutor implements CommandExecutor {

  private static final Logger log = LoggerFactory.getLogger(AnsibleCommandExecutor.class);

  public static CommandExecutor createCommandExecutor() {
    return new AnsibleCommandExecutor();
  }

  @Override
  public ComposableFuture<String> executeCommandAsync(RemoteCommand command) {

    return submit(true, () -> {
      final Process process = new ProcessBuilder(createCommand(command))
        .start();
      process.waitFor(10, TimeUnit.SECONDS);

      return CharStreams.toString(new InputStreamReader(process.getInputStream()));
    });
  }

  private List<String> createCommand(RemoteCommand command) {
    final List<String> cmd = Arrays.asList("ansible", "-m", "raw", hostPattern(command), "-a", command.getExec());

    log.debug("ansible command: [{}]", cmd.stream().collect(Collectors.joining(" ")));
    return cmd;
  }

  private String hostPattern(RemoteCommand command) {
    return command.hosts().stream().collect(Collectors.joining(";"));
  }

  @Override
  public ComposableFuture<String> executeScriptByUrlAsync(RemoteCommand command) {
    return ComposableFutures.fromError(new UnsupportedOperationException("not implemented"));
  }

}
