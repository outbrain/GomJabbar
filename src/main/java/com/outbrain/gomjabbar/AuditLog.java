package com.outbrain.gomjabbar;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Eran Harel
 */
public class AuditLog {

  private final Path logDir;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AuditLog() {
    try {
      logDir = Files.createTempDirectory("GomJabbar_audit");
    } catch (final IOException e) {
      throw new RuntimeException("failed to create audit dir", e);
    }
    System.out.println("Audit Log willl be written to " + logDir.toAbsolutePath());
  }

  public void log(final Fault fault) {
    try (final BufferedWriter writer = Files.newBufferedWriter(Files.createTempFile(logDir, "fault", ".json"))) {
      objectMapper.writeValue(writer, fault);
    } catch (final IOException e) {
      throw new RuntimeException("failed to audit", e);
    }
  }

}
