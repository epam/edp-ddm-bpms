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
import com.epam.digital.data.platform.starter.security.dto.enums.KeycloakPlatformRole;
import com.google.common.collect.Maps;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to create keycloak
 * user.
 */
@RequiredArgsConstructor
@Component(KeycloakCreateOfficerUserDelegate.DELEGATE_NAME)
public class KeycloakCreateOfficerUserDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "keycloakCreateOfficerUserDelegate";

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
  @SystemVariable(name = "userNameResponse", isTransient = true)
  private NamedVariableAccessor<String> userNameResponseVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    userNameResponseVariable.on(execution).set("");

    var fullName = fullNameVariable.from(execution).getOrThrow();
    validateSystemAttribute(fullName, FULL_NAME_REGEX, ATTRIBUTE_FULL_NAME);
    var edrpou = edrpouVariable.from(execution).getOrThrow();
    validateSystemAttribute(edrpou, EDRPOU_REGEX, ATTRIBUTE_EDRPOU);
    var drfo = drfoVariable.from(execution).getOrThrow();
    validateSystemAttribute(drfo, DRFO_REGEX, ATTRIBUTE_DRFO);
    var attributes = attributesVariable.from(execution).getOptional().orElse(Maps.newHashMap());
    validateCustomAttributes(attributes);
    addAttributeIfDefined(attributes, ATTRIBUTE_FULL_NAME, fullName);
    addAttributeIfDefined(attributes, ATTRIBUTE_EDRPOU, edrpou);
    addAttributeIfDefined(attributes, ATTRIBUTE_DRFO, drfo);
    checkUserExistenceByAttributes(idmService, fullName, edrpou, drfo);
    var userName = createUsername(fullName, edrpou, drfo);

    var userRepresentation = new UserRepresentation();
    userRepresentation.setUsername(userName);
    userRepresentation.setFirstName(getFirstNameFromFullName(fullName));
    userRepresentation.setLastName(getLastNameFromFullName(fullName));
    userRepresentation.setEnabled(Boolean.TRUE);
    userRepresentation.setAttributes(mapAttributeValuesToList(attributes));
    //The Keycloak API currently ignores roles when creating a user.
    userRepresentation.setRealmRoles(List.of(KeycloakPlatformRole.OFFICER.getName()));

    // Need to add roles after creating the user.
    var officerRoleRepresentation = idmService.getRoleRepresentations().stream()
        .filter(role -> KeycloakPlatformRole.OFFICER.getName().equals(role.getName()))
        .findFirst()
        .map(Collections::singletonList)
        .orElse(Lists.emptyList());

    idmService.createUserRepresentation(userRepresentation, officerRoleRepresentation);

    userNameResponseVariable.on(execution).set(userName);
  }

  private String createUsername(String fullName, String edrpou, String drfo) {
    var concatenatedStr = fullName.toLowerCase() + edrpou + drfo;
    return DigestUtils.sha256Hex(concatenatedStr.getBytes(StandardCharsets.UTF_8));
  }
}
