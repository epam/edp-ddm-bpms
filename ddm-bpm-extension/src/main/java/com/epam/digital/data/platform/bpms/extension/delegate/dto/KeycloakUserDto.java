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
