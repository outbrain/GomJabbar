package com.outbrain.gomjabbar.audit;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.outbrain.gomjabbar.faults.Fault;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Audit operations facade
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
    System.out.println("Audit Log will be written to " + logDir.toAbsolutePath());
  }

  /**
   * Records a fault execution in the log
   * @param fault entry to log
   */
  public void log(final Fault fault) {
    try (final BufferedWriter writer = Files.newBufferedWriter(Files.createTempFile(logDir, "fault", ""))) {
      objectMapper.writeValue(writer, fault);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to audit", e);
    }
  }

  /**
   * @return a listing of all recorded executions
   */
  public Map<String, Fault> list() {
    try {
      return Files.list(logDir)
        .map(path -> Pair.of(path.getFileName().toString(),
          readFault(path).orElseThrow(() -> new RuntimeException("Failed to read fault entry"))))
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    } catch (final IOException e) {
      throw new RuntimeException("Failed to list audit entries", e);
    }
  }

  private Optional<Fault> readFault(final Path path) {
    try {
      return Optional.of(objectMapper.readValue(path.toFile(), Fault.class));
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves a fault execution by it's id
   * @param faultId the execution token
   * @return the specified fault execution
   */
  public Optional<Fault> findFault(final String faultId) {
    return readFault(Paths.get(logDir.toString(), faultId));
  }

}
