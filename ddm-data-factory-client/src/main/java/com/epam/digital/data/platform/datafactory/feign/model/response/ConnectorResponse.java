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

package com.epam.digital.data.platform.datafactory.feign.model.response;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.camunda.spin.json.SpinJsonNode;

/**
 * The class represents a response that is used to map response from data factory.
 */
@Builder
@Getter
public class ConnectorResponse implements Serializable {

  private final int statusCode;
  private final transient SpinJsonNode responseBody;
  private final Map<String, Collection<String>> headers;
}
