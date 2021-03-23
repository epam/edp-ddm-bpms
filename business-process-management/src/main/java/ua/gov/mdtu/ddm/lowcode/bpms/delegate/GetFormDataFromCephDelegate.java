package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.constants.CamundaDelegateConstants;

@Component("getFormDataFromCephDelegate")
@Logging
public class GetFormDataFromCephDelegate extends AbstractCephDelegate {

  private final FormDataCephService cephService;

  @Autowired
  public GetFormDataFromCephDelegate(ObjectMapper objectMapper,
      FormDataCephService cephService) {
    super(objectMapper);
    this.cephService = cephService;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var taskDefinitionKey = execution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(
        CamundaDelegateConstants.TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var cephKey = (String) execution.getVariable(taskFormDataVariableName);
    var formData = cephService.getFormData(cephKey);

    ((AbstractVariableScope) execution).setVariableLocalTransient("formData", serializeFormData(formData));
  }
}
