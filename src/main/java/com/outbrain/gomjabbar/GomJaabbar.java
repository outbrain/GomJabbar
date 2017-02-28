package com.outbrain.gomjabbar;

import com.outbrain.ob1k.consul.ConsulAPI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Eran Harel
 */
public class GomJaabbar {

  public static void main(final String[] args) throws InterruptedException, ExecutionException, TimeoutException, ParseException, IOException {

    final Options options = new Options();
    options.addOption("a", "authtoken", true, "rundeck auth token");
    options.addOption("r", "rundeckhost", true, "rundeck host");

    final HelpFormatter helpFormatter = new HelpFormatter();
    final CommandLineParser parser = new DefaultParser();
    final CommandLine cmd = parser.parse(options, args);

    final String authToken = parseOption(cmd, options, "a", helpFormatter);
    final String runDeckHost = parseOption(cmd, options, "r", helpFormatter);
    final FaultInjectors faultInjectors = loadFaultInjectors(authToken, runDeckHost);

    selectAndInjectFailures(faultInjectors);
    System.exit(0);
  }

  private static FaultInjectors loadFaultInjectors(final String authToken, final String runDeckHost) {
    // OK, this should load it from somewhere later...
    final RundeckCommandExecutor rundeckCommandExecutor = new RundeckCommandExecutor(authToken, runDeckHost);
    final FaultInjector faultInjector = new DummyRemoteFailureInjector(rundeckCommandExecutor);
//    final FaultInjector faultInjector = new DummyFault();
//    final FaultInjector faultInjector = new InitdStopper(rundeckCommandExecutor);
    return new FaultInjectors(Collections.singleton(faultInjector));
  }

  private static void selectAndInjectFailures(final FaultInjectors faultInjectors) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final AuditLog auditLog = new AuditLog();

    final Set<String> excludedDCs = Collections.singleton("il");
    final Set<String> excludedModules = Collections.emptySet();
    final Set<String> includedServiceTypes = Collections.singleton("ob1k");

    final TargetsCollector targetsCollector = new ConsulBasedTargetsCollector(ConsulAPI.getHealth(), ConsulAPI.getCatalog(), new DefaultTargetsFilter(excludedDCs, excludedModules, includedServiceTypes));

    try (Scanner in = new Scanner(System.in)) {
      while(true) {
        final Target target = selectTarget(targetsCollector);
        final FaultInjector faultInjector = faultInjectors.selectFaultInjector();
        System.out.printf("Activate failure on the following target / failure (Y/N)?\n%s\n%s (%s)\n", target, faultInjector.id(), faultInjector.description());

        final String input = in.next();
        if ("Y".equals(input)) {
          injectFailure(auditLog, target, faultInjector);
        }
      }
    } catch (final NoSuchElementException e) {
      System.out.println("I enjoyed injecting failures for you. See you next week :)");
    }
  }

  private static void injectFailure(final AuditLog auditLog, final Target target, final FaultInjector faultInjector) throws IOException, InterruptedException, ExecutionException, TimeoutException {
    auditLog.log(new Fault(target, faultInjector.id()));
    final String failureExecutionLog = faultInjector.injectFailure(target).get(1, TimeUnit.MINUTES);
    System.out.println("Command completed completed; Execution log:\n" + failureExecutionLog);
    System.out.println("=====================================================================\n");
  }

  private static String parseOption(final CommandLine cmd, final Options options, final String opt, final HelpFormatter helpFormatter) {
    if (!cmd.hasOption(opt)) {
      helpFormatter.printHelp("GomJabbar", options);
      System.exit(1);
    }

    return cmd.getOptionValue(opt);
  }

  private static Target selectTarget(final TargetsCollector targetsCollector) throws InterruptedException, ExecutionException, TimeoutException {
    System.out.println("Selecting targets...");
    return targetsCollector.chooseTarget().get(30, TimeUnit.SECONDS);
  }
}