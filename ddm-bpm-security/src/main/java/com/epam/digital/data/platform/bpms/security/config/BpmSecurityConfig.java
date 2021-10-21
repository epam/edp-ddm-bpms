package com.epam.digital.data.platform.bpms.security.config;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the security configuration.
 */
@Configuration
public class BpmSecurityConfig {

  @Bean
  @Qualifier("camundaAdminImpersonationFactory")
  public CamundaImpersonationFactory camundaAdminImpersonationFactory(
      @Value("${camunda.admin-user-id}") String administratorUserId,
      @Value("${camunda.admin-group-id}") String administratorGroupName) {
    return new CamundaImpersonationFactory(administratorUserId, administratorGroupName);
  }
}
