package com.epam.digital.data.platform.bpms.engine.config;

import com.epam.digital.data.platform.starter.security.SystemRole;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class that represents {@code camunda.registry} properties. Contains two subgroups {@code
 * camunda.registry.officer} and {@code camunda.registry.citizen} that represent registry files with
 * available roles list
 */
@Component
@ConfigurationProperties(prefix = "registry-regulation")
public class CamundaRegistryRoles {

  @Setter
  private RegistryRolesDto officer;
  @Setter
  private RegistryRolesDto citizen;
  @Setter
  @Value("${camunda.admin-group-id:camunda-admin}")
  private String camundaAdminRole;

  @Getter
  private Set<String> availableAuthorizedRoles;

  @PostConstruct
  public void initRegistryRolesSet() {
    var officerStream = getRoleNameStream(officer);
    var citizenStream = getRoleNameStream(citizen);
    var camundaAdminStream = Stream.of(camundaAdminRole);
    var systemRolesStream = Stream.of(SystemRole.getRoleNames());

    this.availableAuthorizedRoles =
        Stream.of(officerStream, citizenStream, camundaAdminStream, systemRolesStream)
            .flatMap(Function.identity())
            .collect(Collectors.toUnmodifiableSet());
  }

  private Stream<String> getRoleNameStream(RegistryRolesDto registryRolesDto) {
    if (Objects.isNull(registryRolesDto)) {
      return Stream.empty();
    }
    return registryRolesDto.getRoles().stream().map(RegistryRoleDto::getName);
  }

  @Getter
  @Setter
  public static class RegistryRolesDto {

    private List<RegistryRoleDto> roles;
  }

  @Getter
  @Setter
  public static class RegistryRoleDto {

    private String name;
    private String description;
  }
}
