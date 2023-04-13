/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak;

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IdmServiceProvider {

  private static final String OFFICER_REALM = "OFFICER";
  private static final String CITIZEN_REALM = "CITIZEN";

  @Qualifier("officer-keycloak-client-service")
  private final IdmService officerIdmService;
  @Qualifier("citizen-keycloak-client-service")
  private final IdmService citizenIdmService;

  public IdmService getIdmService(String realm) {
    switch (realm) {
      case OFFICER_REALM:
        return officerIdmService;
      case CITIZEN_REALM:
        return citizenIdmService;
      default:
        throw new IllegalArgumentException("Realm must be one of ['OFFICER', 'CITIZEN']");
    }
  }

}
