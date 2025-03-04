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

package com.epam.digital.data.platform.bpms.rest.config;

import com.epam.digital.data.platform.bpms.rest.controller.ExtendedAuthorizationController;
import com.epam.digital.data.platform.bpms.rest.controller.ProcessDefinitionController;
import com.epam.digital.data.platform.bpms.rest.controller.ProcessInstanceController;
import com.epam.digital.data.platform.bpms.rest.controller.TaskController;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.CamundaForbiddenOperationExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.CamundaRestExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.CamundaSystemExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.ConstraintViolationExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.KeycloakExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.TaskAlreadyInCompletionExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.UnauthorizedExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.UserDataValidationExceptionMapper;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link CamundaJerseyResourceConfig} that is used to
 * register additional resources.
 */
@Component
public class LowcodeJerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  public void afterPropertiesSet() throws Exception {
    CamundaRestResources.getConfigurationClasses().remove(JacksonConfigurator.class);
    super.afterPropertiesSet();
  }

  @Override
  protected void registerAdditionalResources() {
    register(TaskController.class);
    register(ProcessInstanceController.class);
    register(ProcessDefinitionController.class);
    register(ExtendedAuthorizationController.class);

    register(UserDataValidationExceptionMapper.class);
    register(CamundaSystemExceptionMapper.class);
    register(CamundaRestExceptionMapper.class);
    register(TaskAlreadyInCompletionExceptionMapper.class);
    register(KeycloakExceptionMapper.class);
    register(UnauthorizedExceptionMapper.class);
    register(ConstraintViolationExceptionMapper.class);
    register(CamundaForbiddenOperationExceptionMapper.class);
  }
}
