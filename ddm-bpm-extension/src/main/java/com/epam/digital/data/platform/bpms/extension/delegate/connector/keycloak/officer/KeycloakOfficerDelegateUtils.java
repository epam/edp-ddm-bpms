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

import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.integration.idm.model.SearchUserQuery;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.util.Strings;

public class KeycloakOfficerDelegateUtils {

  private KeycloakOfficerDelegateUtils() {
  }

  public static final String ATTRIBUTE_DRFO = "drfo";
  public static final String ATTRIBUTE_EDRPOU = "edrpou";
  public static final String ATTRIBUTE_FULL_NAME = "fullName";
  public static final String DRFO_REGEX = "^[ \\p{IsCyrillic}\\p{IsLatin}\\d]{1,10}$";
  public static final String EDRPOU_REGEX = "^\\d{8}(?:\\d{2})?$";
  public static final String FULL_NAME_REGEX = "^[ '`’—–\\-\\p{IsCyrillic}\\p{IsLatin}\\d]{1,255}$";
  private static final String CUSTOM_ATTRIBUTE_REGEX = "^[^\\[\\]{}\"\\\\]{1,255}$";
  private static final List<String> SYSTEM_ATTRIBUTES = List.of(ATTRIBUTE_FULL_NAME, ATTRIBUTE_DRFO,
      ATTRIBUTE_EDRPOU);
  private static final String FULL_NAME_SPLIT_REGEX = " ";
  private static final int FULL_NAME_LENGTH_PARTS = 2;
  private static final int FIRST_NAME_INDEX = 1;
  private static final int LAST_NAME_INDEX = 0;


  @SuppressWarnings("unchecked")
  public static Map<String, List<String>> mapAttributeValuesToList(Map<String, Object> attributes) {
    Map<String, List<String>> mappedAttributes = Maps.newHashMap();
    attributes.forEach((key, value) -> {
      var listOfValues = value instanceof List ? (List<String>) value : List.of((String) value);
      mappedAttributes.put(key, listOfValues);
    });
    return mappedAttributes;
  }

  public static String getFirstNameFromFullName(String fullName) {
    var parts = getFullNameParts(fullName);
    return parts.length == FULL_NAME_LENGTH_PARTS ? parts[FIRST_NAME_INDEX] : Strings.EMPTY;
  }

  public static String getLastNameFromFullName(String fullName) {
    var parts = getFullNameParts(fullName);
    return parts[LAST_NAME_INDEX];
  }

  private static String[] getFullNameParts(String fullName) {
    return fullName.split(FULL_NAME_SPLIT_REGEX, FULL_NAME_LENGTH_PARTS);
  }


  public static void addAttributeIfDefined(Map<String, Object> attributes, String key,
      String value) {
    if (Objects.nonNull(value)) {
      attributes.put(key, value);
    }
  }

  public static void validateSystemAttribute(String value, String regex, String attrName) {
    if (Objects.nonNull(value) && !value.matches(regex)) {
      throw new IllegalArgumentException(
          String.format("Value of the Keycloak attribute [%s] do not match the regex: %s",
              attrName, regex));
    }
  }

  public static void validateCustomAttributes(Map<String, Object> attributes) {
    SYSTEM_ATTRIBUTES.forEach(name -> {
      if (attributes.containsKey(name)) {
        throw new IllegalArgumentException(
            String.format("Keycloak attribute [%s] is duplicated", name));
      }
    });

    attributes.forEach((key, value) -> {
      if (!key.matches(CUSTOM_ATTRIBUTE_REGEX) || !isValidCustomAttributeValue(value)) {
        throw new IllegalArgumentException(
            String.format("Keycloak attribute %s: %s do not pass validation", key, value));
      }
    });
  }

  public static void checkUserExistenceByAttributes(IdmService idmService, String fullName,
      String edrpou, String drfo) {
    Map<String, String> queryAttributes = Maps.newHashMap();
    addQueryAttributeIfDefined(queryAttributes, ATTRIBUTE_FULL_NAME, fullName);
    addQueryAttributeIfDefined(queryAttributes, ATTRIBUTE_EDRPOU, edrpou);
    addQueryAttributeIfDefined(queryAttributes, ATTRIBUTE_DRFO, drfo);
    var query = SearchUserQuery.builder()
        .attributes(queryAttributes)
        .build();
    var idmUsers = idmService.searchUsers(query);
    if (!idmUsers.isEmpty()) {
      throw new KeycloakException(
          String.format("Found %s users with the same attributes", idmUsers.size()));
    }
  }

  private static void addQueryAttributeIfDefined(Map<String, String> queryAttributes, String key,
      String value) {
    if (Objects.nonNull(value)) {
      queryAttributes.put(key, value);
    }
  }

  @SuppressWarnings("unchecked")
  private static boolean isValidCustomAttributeValue(Object value) {
    if (value instanceof List) {
      return ((List<String>) value).stream()
          .allMatch(attrValue -> attrValue.matches(CUSTOM_ATTRIBUTE_REGEX));
    } else {
      return ((String) value).matches(CUSTOM_ATTRIBUTE_REGEX);
    }
  }
}
