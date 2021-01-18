package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.constants.CamundaDelegateConstants;

@Component("getFormDataFromCephDelegate")
@RequiredArgsConstructor
@Logging
public class GetFormDataFromCephDelegate implements JavaDelegate {

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    var taskDefinitionKey = execution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(
        CamundaDelegateConstants.TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var cephKey = (String) execution.getVariable(taskFormDataVariableName);
    var formData = cephService.getContent(cephBucketName, cephKey);

    execution.setVariableLocal("formData", formData);
  }
}
