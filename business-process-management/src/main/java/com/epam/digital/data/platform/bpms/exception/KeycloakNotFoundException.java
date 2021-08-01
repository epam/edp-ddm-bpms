package com.epam.digital.data.platform.bpms.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The class represents an exception for keycloak client communication which will be thrown in case
 * of a not found entity(user,role, etc.).
 */
@Getter
@RequiredArgsConstructor
public class KeycloakNotFoundException extends RuntimeException {

  private final String message;
}
