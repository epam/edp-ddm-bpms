package ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums;

/**
 * Enumeration of sort order types
 */
public enum SortOrder {
  ASC, DESC;

  public String stringValue() {
    return name().toLowerCase();
  }
}
