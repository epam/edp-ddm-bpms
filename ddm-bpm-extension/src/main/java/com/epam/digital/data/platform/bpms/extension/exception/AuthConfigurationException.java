package com.epam.digital.data.platform.bpms.extension.exception;

public class AuthConfigurationException extends RuntimeException{

  public AuthConfigurationException(String message) {
    super(message);
  }

  public AuthConfigurationException(String message, Exception cause) {
    super(message, cause);
  }
}
