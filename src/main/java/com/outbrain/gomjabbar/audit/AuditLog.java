package com.outbrain.gomjabbar.audit;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.outbrain.gomjabbar.faults.Fault;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

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
      throw new RuntimeException("Failed to audit", e);
    }
  }

  public Collection<Fault> list() {
    try {
      return Files.list(logDir).map(path -> {
        try {
          return objectMapper.readValue(path.toFile(), Fault.class);
        } catch (IOException e) {
          throw new RuntimeException("Failed to read audit entry " + path, e);
        }
      }).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException("Failed to list audit entries", e);
    }
  }
}
