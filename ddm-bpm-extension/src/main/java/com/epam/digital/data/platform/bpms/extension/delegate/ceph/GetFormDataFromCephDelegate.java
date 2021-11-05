package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from ceph using {@link FormDataCephService}
 * service, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return it.
 */
@Slf4j
@Component(GetFormDataFromCephDelegate.DELEGATE_NAME)
public class GetFormDataFromCephDelegate extends BaseFormDataDelegate {

  public static final String DELEGATE_NAME = "getFormDataFromCephDelegate";

  public GetFormDataFromCephDelegate(FormDataCephService cephService,
      CephKeyProvider cephKeyProvider) {
    super(cephService, cephKeyProvider);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var taskDefinitionKey = taskDefinitionKeyVariable.from(execution).get();

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, execution.getProcessInstanceId());

    log.debug("Start getting form data by key {}", cephKey);
    var formData = cephService.getFormData(cephKey)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());
    log.debug("Got form data by key {}", cephKey);

    formDataVariable.on(execution).set(Spin.JSON(formData));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
