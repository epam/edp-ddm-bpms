package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.controller.StartFormController;
import com.epam.digital.data.platform.bpms.controller.LowCodeDefaultProcessEngineRestController;
import com.epam.digital.data.platform.bpms.controller.TaskPropertyController;
import com.epam.digital.data.platform.bpms.exception.mapper.CamundaRestExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.CamundaSystemExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.TaskAlreadyInCompletionExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.UserDataValidationExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.engine.rest.impl.JaxRsTwoNamedProcessEngineRestServiceImpl;
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link CamundaJerseyResourceConfig} that is used to
 * register additional resources.
 */
@Slf4j
@Component
public class LowcodeJerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerCamundaRestResources() {
    log.info("Configuring camunda rest api.");

    this.register(JaxRsTwoNamedProcessEngineRestServiceImpl.class);
    this.register(LowCodeDefaultProcessEngineRestController.class);

    this.registerClasses(CamundaRestResources.getConfigurationClasses());
    this.register(JacksonFeature.class);

    log.info("Finished configuring camunda rest api.");
  }

  @Override
  protected void registerAdditionalResources() {
    register(TaskPropertyController.class);
    register(StartFormController.class);

    register(UserDataValidationExceptionMapper.class);
    register(CamundaSystemExceptionMapper.class);
    register(CamundaRestExceptionMapper.class);
    register(TaskAlreadyInCompletionExceptionMapper.class);
  }
}
