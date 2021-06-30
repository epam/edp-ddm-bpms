package com.epam.digital.data.platform.bpms.delegate.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The class represents a user obtained from the keycloak.
 */
@Getter
@AllArgsConstructor
public class KeycloakUserDto implements Serializable {

  private final String userName;
  private final String fullName;
}
