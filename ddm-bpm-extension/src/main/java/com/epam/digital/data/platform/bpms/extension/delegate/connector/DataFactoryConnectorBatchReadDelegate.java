package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.List;
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

  @SystemVariable(name = "resourceIds")
  private NamedVariableAccessor<List<String>> resourceIdsVariable;

  @Autowired
  public DataFactoryConnectorBatchReadDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName, dataFactoryBaseUrl);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var resourceIds = resourceIdsVariable.from(execution).getOrDefault(List.of());

    logProcessExecution("batch read entities on resource", resource);
    var response = executeBatchGetOperation(execution, resource, resourceIds);

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse executeBatchGetOperation(DelegateExecution execution,
      String resource, List<String> resourceIds) {
    var json = Spin.JSON("[]");

    resourceIds.stream()
        .map(id -> performGet(execution, resource, id))
        .map(ConnectorResponse::getResponseBody).forEach(json::append);

    return ConnectorResponse.builder()
        .statusCode(HttpStatus.OK.value())
        .responseBody(json)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
