package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.controller.TaskPropertyController;
import com.epam.digital.data.platform.bpms.exception.mapper.CamundaRestExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.CamundaSystemExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.TaskAlreadyInCompletionExceptionMapper;
import com.epam.digital.data.platform.bpms.exception.mapper.UserDataValidationExceptionMapper;
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link CamundaJerseyResourceConfig} that is used to
 * register additional resources.
 */
@Component
public class LowcodeJerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerAdditionalResources() {
    register(TaskPropertyController.class);

    register(UserDataValidationExceptionMapper.class);
    register(CamundaSystemExceptionMapper.class);
    register(CamundaRestExceptionMapper.class);
    register(TaskAlreadyInCompletionExceptionMapper.class);
  }
}
