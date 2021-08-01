package com.epam.digital.data.platform.bpms.api.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of platform http headers
 */
@Getter
@AllArgsConstructor
public enum PlatformHttpHeader {

  X_ACCESS_TOKEN("X-Access-Token"),
  X_SOURCE_SYSTEM("X-Source-System"),
  X_SOURCE_APPLICATION("X-Source-Application"),
  X_SOURCE_BUSINESS_PROCESS("X-Source-Business-Process"),
  X_SOURCE_BUSINESS_ACTIVITY("X-Source-Business-Activity"),
  X_DIGITAL_SIGNATURE("X-Digital-Signature"),
  X_DIGITAL_SIGNATURE_DERIVED("X-Digital-Signature-Derived"),
  X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID("X-Source-Business-Process-Instance-Id"),
  X_SOURCE_BUSINESS_PROCESS_DEFINITION_ID("X-Source-Business-Process-Definition-Id"),
  X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID("X-Source-Business-Activity-Instance-Id");

  private final String name;
}
