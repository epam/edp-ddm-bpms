package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.constants.CamundaDelegateConstants;

@Component("putFormDataToCephDelegate")
@Logging
public class PutFormDataToCephDelegate extends AbstractCephDelegate {

  private final FormDataCephService cephService;

  @Autowired
  public PutFormDataToCephDelegate(ObjectMapper objectMapper,
      FormDataCephService cephService) {
    super(objectMapper);
    this.cephService = cephService;
  }

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var taskDefinitionKey = (String) delegateExecution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(
        CamundaDelegateConstants.TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var processInstanceId = delegateExecution.getProcessInstanceId();
    var cephKey = String.format(CamundaDelegateConstants.TASK_FORM_DATA_VALUE_FORMAT,
        processInstanceId, taskFormDataVariableName);
    var formData = (String) delegateExecution.getVariable("formData");

    cephService.putFormData(cephKey, deserializeFormData(formData));
    delegateExecution.setVariable(taskFormDataVariableName, cephKey);
  }
}
