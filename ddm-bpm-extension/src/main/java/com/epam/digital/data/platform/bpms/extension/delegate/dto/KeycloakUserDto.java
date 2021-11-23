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

package com.epam.digital.data.platform.bpms.extension.delegate.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The class represents a user obtained from the keycloak.
 */
@ToString
@Getter
@AllArgsConstructor
public class KeycloakUserDto implements Serializable {

  private final String userName;
  private final String fullName;
}
