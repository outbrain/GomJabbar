package com.outbrain.gomjabbar;

import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.http.HttpClient;
import com.outbrain.ob1k.http.RequestBuilder;
import com.outbrain.ob1k.http.common.ContentType;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Eran Harel
 */
public class InitdStopper implements FaultInjector {

  private final String rundeckBaseUrl;
  private final String authToken;

  private final HttpClient httpClient;

  public InitdStopper(final String authToken, final String rundeckHost) {
    this.authToken = authToken;
    rundeckBaseUrl = String.format("https://%s:4443/api", rundeckHost);
    httpClient = new HttpClient.Builder()
      .setAcceptAnySslCertificate(true)
      .setConnectionTimeout(1000)
      .setReadTimeout(10000)
      .setRequestTimeout(10000)
      .build();
  }

  @Override
  public void injectFailure(final Target target) {
    final RundeckCommand command = new RundeckCommand(target.getHost(), String.format("sudo service %s stop", target.getModule()));

    final RequestBuilder postBuilder = httpClient.post(String.format("%s/14/project/ops/run/command?authtoken=%s", rundeckBaseUrl, authToken));
    postBuilder.setContentType(ContentType.JSON)
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .setBody(command)
      .asValue(RundeckCommandResponse.class)
      .consume(result -> {
        if (result.isSuccess()) {
          System.out.println(result.getValue());
          monitorExecution(result.getValue().getExecution().getId());
        } else {
          result.getError().printStackTrace();
        }
    });

  }

  private void monitorExecution(final long id) {
    httpClient.get(String.format("%s/10/execution/%d/output?authtoken=%s&format=json", rundeckBaseUrl, id, authToken))
      .addHeader("Accept", ContentType.JSON.responseEncoding())
      .asValue(RundeckExecutionStatus.class)
      .consume(result -> {
        if (result.isSuccess()) {
          if (result.getValue().isCompleted()) {
            System.out.println("Stop completed; Execution log:");
            for (final RundeckExecutionStatus.Entry entry : result.getValue().getEntries()) {
              System.out.println(entry.getLog());
            }

            System.exit(0);
          } else {
            System.out.println("Still running...");
            ComposableFutures.schedule((Callable<Void>) () -> {
              monitorExecution(id);
              return null;
            }, 1, TimeUnit.SECONDS);
          }
        } else {
          System.err.println("Failed to monitor failure task execution...");
          result.getError().printStackTrace();
          System.exit(0);
        }
      });
  }
}
