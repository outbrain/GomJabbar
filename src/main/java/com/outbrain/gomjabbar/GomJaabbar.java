package com.outbrain.gomjabbar;

import com.outbrain.ob1k.consul.ConsulAPI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Eran Harel
 */
public class GomJaabbar {
  public static void main(final String[] args) throws InterruptedException, ExecutionException, TimeoutException, ParseException {

    final Options options = new Options();
    options.addOption("a", "authtoken", true, "rundeck auth token");
    options.addOption("r", "rundeckhost", true, "rundeck host");

    final HelpFormatter helpFormatter = new HelpFormatter();
    final CommandLineParser parser = new DefaultParser();
    final CommandLine cmd = parser.parse(options, args);

    final String authToken = parseOption(cmd, options, "a", helpFormatter);
    final String runDeckHost = parseOption(cmd, options, "r", helpFormatter);


    selectAndKillTarget(authToken, runDeckHost);
  }

  private static void selectAndKillTarget(final String authToken, final String runDeckHost) throws InterruptedException, ExecutionException, TimeoutException {
    final Set<String> excludedDCs = Collections.singleton("il");
    final Set<String> excludedModules = Collections.emptySet();
    final Set<String> includedServiceTypes = Collections.singleton("ob1k");

    final TargetsCollector targetsCollector = new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), new DefaultTargetsFilter(excludedDCs, excludedModules, includedServiceTypes));
    Target target = selectTarget(targetsCollector);

    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);
//    final FaultInjector faultInjector = new DummyRemoteFailureInjector(rundeckCommandExecutor);
//    final FaultInjector faultInjector = new DummyFault();
    final FaultInjector faultInjector = new InitdStopper(rundeckCommandExecutor);

    try (Scanner in = new Scanner(System.in)) {

      while (in.hasNext()) {
        final String input = in.next();
        if ("Y".equals(input)) {
          faultInjector.injectFailure(target);
        }

        target = selectTarget(targetsCollector);
      }
    }

    System.exit(0);
  }

  private static String parseOption(final CommandLine cmd, final Options options, final String opt, final HelpFormatter helpFormatter) {
    if (!cmd.hasOption(opt)) {
      helpFormatter.printHelp("GomJabbar", options);
      System.exit(1);
    }

    return cmd.getOptionValue(opt);
  }

  private static Target selectTarget(final TargetsCollector targetsCollector) throws InterruptedException, ExecutionException, TimeoutException {
    final Target target = targetsCollector.chooseTarget().get(10, TimeUnit.SECONDS);
    System.out.printf("Activate failure on the following target (Y/N)?\n%s\n", target);
    return target;
  }
}