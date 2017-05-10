package com.outbrain.gomjabbar.execution;

import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.http.HttpClient;
import com.outbrain.ob1k.http.RequestBuilder;
import com.outbrain.ob1k.http.common.ContentType;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Eran Harel
 */
public class RundeckCommandExecutor implements CommandExecutor {

  private final String rundeckBaseUrl;
  private final String authToken;

  private final HttpClient httpClient;

  private RundeckCommandExecutor(final String authToken, final String rundeckHost) {
    this.authToken = authToken;
    rundeckBaseUrl = String.format("https://%s:4443/api", rundeckHost);
    httpClient = new HttpClient.Builder()
      .setAcceptAnySslCertificate(true)
      .setConnectionTimeout(1000)
      .setReadTimeout(10000)
      .setRequestTimeout(10000)
      .build();
  }

  public static CommandExecutor createCommandExecutor() {
    final String authToken = System.getProperty("com.outbrain.gomjabbar.rundeckAuthToken");
    final String runDeckHost = System.getProperty("com.outbrain.gomjabbar.rundeckHost");

    return new RundeckCommandExecutor(authToken, runDeckHost);
  }

  @Override
  public ComposableFuture<String> executeCommandAsync(final RemoteCommand command) {
    return executeRemoteAsync(command, "command");
  }

  @Override
  public ComposableFuture<String> executeScriptByUrlAsync(final RemoteCommand command) {
    return executeRemoteAsync(command, "url");
  }

  private ComposableFuture<String> executeRemoteAsync(final RemoteCommand command, final String type) {
    final RequestBuilder postBuilder = httpClient.post(String.format("%s/14/project/ops/run/%s?authtoken=%s", rundeckBaseUrl, type, authToken));
    return postBuilder.setContentType(ContentType.JSON)
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .setBody(command)
      .asValue(RundeckCommandResponse.class)
      .flatMap(response -> {
          System.out.println("Failure was triggered successfully: " + response);
          return monitorExecution(response.getExecution().getId());
        });
  }

  private ComposableFuture<String> monitorExecution(final long id) {
    final ComposableFuture<RundeckExecutionStatus> monitorFuture = httpClient.get(String.format("%s/10/execution/%d/output?authtoken=%s&format=json", rundeckBaseUrl, id, authToken))
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .asValue(RundeckExecutionStatus.class);

    return monitorFuture.alwaysWith(result -> {
        if (result.isSuccess()) {
          if (result.getValue().isCompleted()) {
            final String executionLog = result.getValue().getEntries()
              .stream()
              .map(RundeckExecutionStatus.Entry::getLog)
              .collect(Collectors.joining("\n"));

            return ComposableFutures.fromValue(executionLog);
          } else {
            System.out.println("Waiting for command completion...");
            return ComposableFutures.scheduleFuture(() -> monitorExecution(id), 1, TimeUnit.SECONDS);
          }
        } else {
          System.err.println("Failed to monitor failure task execution...");
          return ComposableFutures.fromError(result.getError());
        }
      });
  }

}
