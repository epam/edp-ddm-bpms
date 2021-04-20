package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("dataFactoryConnectorBatchReadDelegate")
public class DataFactoryConnectorBatchReadDelegate extends DataFactoryConnectorReadDelegate {

  private static final String VAR_RESOURCE_IDS = "resourceIds";

  @Autowired
  public DataFactoryConnectorBatchReadDelegate(RestTemplate restTemplate,
      FormDataCephService formDataCephService, CephKeyProvider cephKeyProvider,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, formDataCephService, cephKeyProvider, springAppName, dataFactoryBaseUrl);
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var resourceIds = (List<String>) execution.getVariable(VAR_RESOURCE_IDS);

    var response = executeBatchGetOperation(execution, resource, resourceIds);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse executeBatchGetOperation(DelegateExecution execution,
      String resource, List<String> resourceIds) {
    var json = Spin.JSON("[]");

    resourceIds.stream()
        .map(id -> performGet(execution, resource, id))
        .map(DataFactoryConnectorResponse::getResponseBody).forEach(json::append);

    return DataFactoryConnectorResponse.builder()
        .statusCode(HttpStatus.OK.value())
        .responseBody(json)
        .build();
  }
}
