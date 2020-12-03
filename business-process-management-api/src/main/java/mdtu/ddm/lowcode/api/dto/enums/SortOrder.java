package mdtu.ddm.lowcode.api.dto.enums;

public enum SortOrder {
  ASC, DESC;

  public String stringValue() {
    return name().toLowerCase();
  }
}
