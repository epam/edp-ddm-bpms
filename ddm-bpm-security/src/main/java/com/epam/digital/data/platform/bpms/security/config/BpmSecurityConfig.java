/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.security.config;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * The class represents a holder for beans of the security configuration.
 */
@Configuration
public class BpmSecurityConfig {

  @Bean
  @RequestScope
  @Qualifier("camundaAdminImpersonation")
  public CamundaImpersonation camundaAdminImpersonation(
      @Value("${camunda.admin-user-id}") String administratorUserId,
      @Value("${camunda.admin-group-id}") String administratorGroupName,
      ProcessEngine processEngine) {
    var impersonatee = new Authentication(administratorUserId, List.of(administratorGroupName));
    return new CamundaImpersonation(processEngine, impersonatee);
  }

  @Bean
  @Qualifier("camundaAdminImpersonationFactory")
  public CamundaImpersonationFactory camundaAdminImpersonationFactory(
      @Value("${camunda.admin-user-id}") String administratorUserId,
      @Value("${camunda.admin-group-id}") String administratorGroupName) {
    return new CamundaImpersonationFactory(administratorUserId, administratorGroupName);
  }
}
