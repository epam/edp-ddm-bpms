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

import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.ATTRIBUTE_DRFO;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.ATTRIBUTE_EDRPOU;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.ATTRIBUTE_FULL_NAME;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.DRFO_REGEX;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.EDRPOU_REGEX;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.FULL_NAME_REGEX;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.addAttributeIfDefined;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.checkUserExistenceByAttributes;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.getFirstNameFromFullName;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.getLastNameFromFullName;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.mapAttributeValuesToList;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.validateCustomAttributes;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakOfficerDelegateUtils.validateSystemAttribute;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to update keycloak
 * user.
 */
@RequiredArgsConstructor
@Component(KeycloakSaveOfficerUserAttributesDelegate.DELEGATE_NAME)
public class KeycloakSaveOfficerUserAttributesDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakSaveOfficerUserAttributesDelegate";

  @Qualifier("officer-keycloak-client-service")
  private final IdmService idmService;

  @SystemVariable(name = "fullName")
  private NamedVariableAccessor<String> fullNameVariable;
  @SystemVariable(name = "drfo")
  private NamedVariableAccessor<String> drfoVariable;
  @SystemVariable(name = "edrpou")
  private NamedVariableAccessor<String> edrpouVariable;
  @SystemVariable(name = "attributes")
  private NamedVariableAccessor<Map<String, Object>> attributesVariable;
  @SystemVariable(name = "username")
  private NamedVariableAccessor<String> usernameVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var userName = usernameVariable.from(execution).getOrThrow();
    var fullName = fullNameVariable.from(execution).get();
    validateSystemAttribute(fullName, FULL_NAME_REGEX, ATTRIBUTE_FULL_NAME);
    var edrpou = edrpouVariable.from(execution).get();
    validateSystemAttribute(edrpou, EDRPOU_REGEX, ATTRIBUTE_EDRPOU);
    var drfo = drfoVariable.from(execution).get();
    validateSystemAttribute(drfo, DRFO_REGEX, ATTRIBUTE_DRFO);
    var attributes = attributesVariable.from(execution).getOptional().orElse(Maps.newHashMap());
    validateCustomAttributes(attributes);

    var userRepresentation = idmService.getUserRepresentationByUserName(userName);
    var currentAttr = userRepresentation.getAttributes();
    var currentUserFullName = getValueFromCurrentAttribute(currentAttr, ATTRIBUTE_FULL_NAME);
    var currentUserEdrpou = getValueFromCurrentAttribute(currentAttr, ATTRIBUTE_EDRPOU);
    var currentUserDrfo = getValueFromCurrentAttribute(currentAttr, ATTRIBUTE_DRFO);

    var fullNameForSearch = Objects.isNull(fullName) ? currentUserFullName : fullName;
    var edrpouForSearch = Objects.isNull(edrpou) ? currentUserEdrpou : edrpou;
    var drfoForSearch = Objects.isNull(drfo) ? currentUserDrfo : drfo;

    var isSearchSystemAttributesEqualsCurrentUserSystemAttributes =
        Objects.equals(fullNameForSearch, currentUserFullName) && Objects.equals(edrpouForSearch,
            currentUserEdrpou) && Objects.equals(drfoForSearch, currentUserDrfo);
    if (!isSearchSystemAttributesEqualsCurrentUserSystemAttributes) {
      checkUserExistenceByAttributes(idmService, fullNameForSearch, edrpouForSearch, drfoForSearch);
    }

    addAttributeIfDefined(attributes, ATTRIBUTE_FULL_NAME, fullName);
    addAttributeIfDefined(attributes, ATTRIBUTE_EDRPOU, edrpou);
    addAttributeIfDefined(attributes, ATTRIBUTE_DRFO, drfo);
    addUserDetailsIfFullNameDefined(fullName, userRepresentation);
    var mappedAttributes = mapAttributeValuesToList(attributes);
    addUserAttributes(mappedAttributes, userRepresentation);

    idmService.updateUserRepresentation(userRepresentation);
  }

  private String getValueFromCurrentAttribute(Map<String, List<String>> attributes, String key) {
    var attr = attributes.get(key);
    if (Objects.nonNull(attr) && !attr.isEmpty()) {
      return attr.get(0);
    }
    return null;
  }

  private void addUserDetailsIfFullNameDefined(String fullName, UserRepresentation user) {
    if (Objects.nonNull(fullName)) {
      var lastName = getLastNameFromFullName(fullName);
      var firstName = getFirstNameFromFullName(fullName);
      user.setFirstName(firstName);
      user.setLastName(lastName);
    }
  }

  private void addUserAttributes(Map<String, List<String>> attributes, UserRepresentation user) {
    if (Objects.isNull(user.getAttributes())) {
      user.setAttributes(Maps.newHashMap());
    }
    user.getAttributes().putAll(attributes);
  }
}
