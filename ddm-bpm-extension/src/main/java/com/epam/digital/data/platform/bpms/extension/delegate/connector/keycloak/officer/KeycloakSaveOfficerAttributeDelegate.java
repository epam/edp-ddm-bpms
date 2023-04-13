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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @deprecated use {@link KeycloakSaveOfficerUserAttributesDelegate} instead
 */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class KeycloakSaveOfficerAttributeDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakSaveOfficerAttributeDelegate";

  @Qualifier("officer-system-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "username")
  private NamedVariableAccessor<String> username;
  @SystemVariable(name = "attributeName")
  private NamedVariableAccessor<String> attributeName;
  @SystemVariable(name = "attributeValue")
  private NamedVariableAccessor<List<String>> attributeValue;

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var username = this.username.from(execution).get();
    var attributeName = this.attributeName.from(execution).get();
    var attributeValue = this.attributeValue.from(execution).get();

    idmService.saveUserAttribute(username, attributeName, attributeValue);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
