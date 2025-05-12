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

package com.epam.digital.data.platform.bpm.history.kafka.util;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

public class KafkaHeaderBuilder {

  private final List<Header> headers = new ArrayList<>();
  private static final String DEFAULT_SOURCE_SYSTEM = "Low-code Platform";

  public KafkaHeaderBuilder withDefaultSourceSystem() {
    headers.add(create(PlatformHttpHeader.X_SOURCE_SYSTEM.getName(), DEFAULT_SOURCE_SYSTEM));
    return this;
  }

  public KafkaHeaderBuilder withSourceApplication(String springAppName) {
    headers.add(create(PlatformHttpHeader.X_SOURCE_APPLICATION.getName(), springAppName));
    return this;
  }

  public KafkaHeaderBuilder add(String key, String value) {
    if (value != null) {
      headers.add(create(key, value));
    }
    return this;
  }

  public List<Header> build() {
    return headers;
  }

  private Header create(String key, String value) {
    return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
  }
}
