package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;

@Component
@RequiredArgsConstructor
public class PutFormDataToCephDelegate implements JavaDelegate {

  private static final String TASK_FORM_DATA_STRING_FORMAT = "secure-sys-var-ref-task-form-data-%s";
  private static final String TASK_FORM_DATA_VALUE_FORMAT = "lowcode-%s-%s";

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var taskDefinitionKey = (String) delegateExecution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var processInstanceId = delegateExecution.getProcessInstanceId();
    var cephKey = String
        .format(TASK_FORM_DATA_VALUE_FORMAT, processInstanceId, taskFormDataVariableName);
    var formData = (String) delegateExecution.getVariable("formData");

    cephService.putContent(cephBucketName, cephKey, formData);
    delegateExecution.setVariable(taskFormDataVariableName, cephKey);
  }
}
