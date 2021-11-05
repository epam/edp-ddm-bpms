package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from ceph using {@link FormDataCephService}
 * service for start form, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return
 * it.
 */
@Slf4j
@Component(GetStartFormDataFromCephDelegate.DELEGATE_NAME)
public class GetStartFormDataFromCephDelegate extends BaseFormDataDelegate {

  public static final String DELEGATE_NAME = "getStartFormDataFromCephDelegate";

  private final StartFormCephKeyVariable startFormCephKeyVariable;

  public GetStartFormDataFromCephDelegate(FormDataCephService cephService,
      CephKeyProvider cephKeyProvider, StartFormCephKeyVariable startFormCephKeyVariable) {
    super(cephService, cephKeyProvider);
    this.startFormCephKeyVariable = startFormCephKeyVariable;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var cephKey = startFormCephKeyVariable.from(execution).get();

    log.debug("Start getting start form data by key {}", cephKey);
    var formData = cephService.getFormData(cephKey)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());
    log.debug("Got start form data by key {}", cephKey);

    formDataVariable.on(execution).set(Spin.JSON(formData));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
