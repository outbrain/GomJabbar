package com.outbrain.gomjabbar.faults;

import java.util.Objects;

/**
 * @author Eran Harel
 */
public class BaseFaultData {
  public final String id;
  public final String description;

  public BaseFaultData(final String id, final String description) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.description = description == null ? getClass().getName() : description;
  }

  public BaseFaultData(final BaseFaultData baseFaultData) {
    this(baseFaultData.id, baseFaultData.description);
  }
}