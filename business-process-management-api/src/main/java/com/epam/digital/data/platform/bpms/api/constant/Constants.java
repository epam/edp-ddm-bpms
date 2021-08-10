package com.epam.digital.data.platform.bpms.api.constant;

/**
 * Class that represents public bpms constants for clients.
 */
public final class Constants {

  /**
   * Name of the variable that contains Ceph key of start form document.
   */
  public static final String BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME = "start_form_ceph_key";
  /**
   * Format of the start form document Ceph key. Has to be used in {@code String.format(String
   * format, String... args)}
   */
  public static final String BPMS_START_FORM_CEPH_KEY_VARIABLE_FORMAT = "lowcode_%s_start_form_%s";

  /**
   * Format of the system variables of the process instance.
   */
  public static final String SYS_VAR_PREFIX_LIKE = "sys-var-%";
  public static final String SYS_VAR_PROCESS_COMPLETION_RESULT = "sys-var-process-completion-result";
  public static final String SYS_VAR_PROCESS_EXCERPT_ID = "sys-var-process-excerpt-id";

  private Constants() {
  }
}
