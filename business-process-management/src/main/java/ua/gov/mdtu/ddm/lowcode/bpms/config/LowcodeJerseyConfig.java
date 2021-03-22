package ua.gov.mdtu.ddm.lowcode.bpms.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.controller.TaskPropertyController;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper.CamundaRestExceptionMapper;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper.CamundaSystemExceptionMapper;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper.UserDataValidationExceptionMapper;

@Component
public class LowcodeJerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerAdditionalResources() {
    register(TaskPropertyController.class);
    register(UserDataValidationExceptionMapper.class);
    register(CamundaSystemExceptionMapper.class);
    register(CamundaRestExceptionMapper.class);
  }
}
