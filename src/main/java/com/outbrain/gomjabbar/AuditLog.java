package com.outbrain.gomjabbar;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Eran Harel
 */
public class AuditLog {

  private final Path logDir;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AuditLog() throws IOException {
    logDir = Files.createTempDirectory("GomJabbar_audit");
    System.out.println("Audit Log willl be written to " + logDir.toAbsolutePath());
  }

  public void log(final Fault fault) throws IOException {
    try (final BufferedWriter writer = Files.newBufferedWriter(Files.createTempFile(logDir, "fault", ".json"))) {
      objectMapper.writeValue(writer, fault);
    }
  }

  public static void main(String[] args) throws IOException {
    Target target = new Target("host", "module", "ob1k", 9, Collections.emptySet());
    Fault fault = new Fault(target, AuditLog.class.getSimpleName());
    new AuditLog().log(fault);
  }
}
