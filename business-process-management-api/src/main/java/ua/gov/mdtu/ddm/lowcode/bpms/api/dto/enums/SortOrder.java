package ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums;

public enum SortOrder {
  ASC, DESC;

  public String stringValue() {
    return name().toLowerCase();
  }
}
