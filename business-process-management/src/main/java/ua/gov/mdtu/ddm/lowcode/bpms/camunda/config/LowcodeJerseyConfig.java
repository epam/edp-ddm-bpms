package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper.UserDataValidationExceptionMapper;

@Component
public class LowcodeJerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerAdditionalResources() {
    register(UserDataValidationExceptionMapper.class);
  }
}
