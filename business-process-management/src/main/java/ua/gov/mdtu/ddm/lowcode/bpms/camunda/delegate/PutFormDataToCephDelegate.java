package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.constants.CamundaDelegateConstants;

@Component("putFormDataToCephDelegate")
@RequiredArgsConstructor
@Logging
public class PutFormDataToCephDelegate implements JavaDelegate {

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var taskDefinitionKey = (String) delegateExecution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(
        CamundaDelegateConstants.TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var processInstanceId = delegateExecution.getProcessInstanceId();
    var cephKey = String.format(CamundaDelegateConstants.TASK_FORM_DATA_VALUE_FORMAT,
        processInstanceId, taskFormDataVariableName);
    var formData = (String) delegateExecution.getVariable("formData");

    cephService.putContent(cephBucketName, cephKey, formData);
    delegateExecution.setVariable(taskFormDataVariableName, cephKey);
  }
}
