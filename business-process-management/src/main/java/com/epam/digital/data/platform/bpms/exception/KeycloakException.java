package com.epam.digital.data.platform.bpms.exception;

/**
 * The class represents an exception for keycloak client communication which will be thrown in case
 * of a not found entity(user,role, etc.).
 */
public class KeycloakException extends RuntimeException {

  public KeycloakException(String message) {
    super(message);
  }

  public KeycloakException(String message, Exception cause) {
    super(message, cause);
  }
}
