package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component(DataFactoryConnectorBatchReadDelegate.DELEGATE_NAME)
public class DataFactoryConnectorBatchReadDelegate extends DataFactoryConnectorReadDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorBatchReadDelegate";

  private static final String VAR_RESOURCE_IDS = "resourceIds";

  @Autowired
  public DataFactoryConnectorBatchReadDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName, dataFactoryBaseUrl);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var resourceIds = (List<String>) execution.getVariable(VAR_RESOURCE_IDS);

    var response = executeBatchGetOperation(execution, resource, resourceIds);

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(RESOURCE_VARIABLE, VAR_RESOURCE_IDS),
        Set.of(RESPONSE_VARIABLE));
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

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
