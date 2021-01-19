package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.controller.TaskPropertyController;

@Component
public class JerseyConfig extends CamundaJerseyResourceConfig {

  @Override
  protected void registerAdditionalResources() {
    register(TaskPropertyController.class);
  }
}
