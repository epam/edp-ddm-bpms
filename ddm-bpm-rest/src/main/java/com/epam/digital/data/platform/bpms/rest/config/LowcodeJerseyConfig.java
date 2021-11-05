package com.epam.digital.data.platform.bpms.rest.config;

import com.epam.digital.data.platform.bpms.rest.controller.HistoricTaskController;
import com.epam.digital.data.platform.bpms.rest.controller.StartFormController;
import com.epam.digital.data.platform.bpms.rest.controller.TaskController;
import com.epam.digital.data.platform.bpms.rest.controller.TaskPropertyController;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.CamundaRestExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.CamundaSystemExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.KeycloakExceptionMapper;
import com.epam.digital.data.platform.bpms.rest.exception.mapper.TaskAlreadyInCompletionExceptionMapper;
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
    register(TaskPropertyController.class);
    register(StartFormController.class);
    register(TaskController.class);
    register(HistoricTaskController.class);

    register(UserDataValidationExceptionMapper.class);
    register(CamundaSystemExceptionMapper.class);
    register(CamundaRestExceptionMapper.class);
    register(TaskAlreadyInCompletionExceptionMapper.class);
    register(KeycloakExceptionMapper.class);
  }
}
