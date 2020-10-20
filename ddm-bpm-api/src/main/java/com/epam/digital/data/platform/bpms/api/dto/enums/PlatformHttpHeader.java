/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID("X-Source-Business-Activity-Instance-Id"),
  X_XSRF_TOKEN("X-XSRF-TOKEN");

  private final String name;
}