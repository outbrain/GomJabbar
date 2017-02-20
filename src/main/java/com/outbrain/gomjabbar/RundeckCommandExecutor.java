package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.http.HttpClient;
import com.outbrain.ob1k.http.RequestBuilder;
import com.outbrain.ob1k.http.common.ContentType;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Eran Harel
 */
public class RundeckCommandExecutor {

  private final String rundeckBaseUrl;
  private final String authToken;

  private final HttpClient httpClient;

  public RundeckCommandExecutor(final String authToken, final String rundeckHost) {
    this.authToken = authToken;
    rundeckBaseUrl = String.format("https://%s:4443/api", rundeckHost);
    httpClient = new HttpClient.Builder()
      .setAcceptAnySslCertificate(true)
      .setConnectionTimeout(1000)
      .setReadTimeout(10000)
      .setRequestTimeout(10000)
      .build();
  }

  public void executeCommand(final RundeckCommand command) {
    final RequestBuilder postBuilder = httpClient.post(String.format("%s/14/project/ops/run/command?authtoken=%s", rundeckBaseUrl, authToken));
    final ComposableFuture<?> rundeckCommandResponseFuture = postBuilder.setContentType(ContentType.JSON)
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .setBody(command)
      .asValue(RundeckCommandResponse.class)
      .always(result -> {
        if (result.isSuccess()) {
          System.out.println("Failure was triggered successfully: " + result.getValue());
          return monitorExecution(result.getValue().getExecution().getId());
        } else {
          return ComposableFutures.fromError(result.getError());
        }
      });

    try {
      rundeckCommandResponseFuture.get(1, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }
  }

  private ComposableFuture<?> monitorExecution(final long id) {
    final ComposableFuture<RundeckExecutionStatus> monitorFuture = httpClient.get(String.format("%s/10/execution/%d/output?authtoken=%s&format=json", rundeckBaseUrl, id, authToken))
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .asValue(RundeckExecutionStatus.class);

    return monitorFuture.always(result -> {
        if (result.isSuccess()) {
          if (result.getValue().isCompleted()) {
            System.out.println("Command completed completed; Execution log:");
            for (final RundeckExecutionStatus.Entry entry : result.getValue().getEntries()) {
              System.out.println(entry.getLog());
            }
            System.out.println("=====================================================================\n");

            return ComposableFutures.fromValue(result.getValue().getEntries());
          } else {
            System.out.println("Waiting for command completion...");
            return ComposableFutures.schedule((Callable<Void>) () -> {
              monitorExecution(id);
              return null;
            }, 1, TimeUnit.SECONDS);
          }
        } else {
          System.err.println("Failed to monitor failure task execution...");
          return ComposableFutures.fromError(result.getError());
        }
      });
  }

}
