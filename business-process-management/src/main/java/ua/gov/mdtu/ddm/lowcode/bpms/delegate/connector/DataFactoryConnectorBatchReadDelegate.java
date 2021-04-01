package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;

@Component("dataFactoryConnectorBatchReadDelegate")
public class DataFactoryConnectorBatchReadDelegate extends DataFactoryConnectorReadDelegate {

  private static final String VAR_RESOURCE_IDS = "resourceIds";

  private final ObjectMapper objectMapper;

  @Autowired
  public DataFactoryConnectorBatchReadDelegate(RestTemplate restTemplate,
      FormDataCephService formDataCephService, ObjectMapper objectMapper,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, formDataCephService, springAppName, dataFactoryBaseUrl);
    this.objectMapper = objectMapper;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var resourceIds = (List<String>) execution.getVariable(VAR_RESOURCE_IDS);

    var response = executeBatchGetOperation(execution, resource, resourceIds);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse executeBatchGetOperation(DelegateExecution execution, String resource, List<String> resourceIds) {
    List<Map<String, Object>> entities = resourceIds.stream()
        .map(id -> performGet(execution, resource, id))
        .map(this::parseBody).collect(Collectors.toList());

    return DataFactoryConnectorResponse.builder()
        .statusCode(HttpStatus.OK.value())
        .responseBody(serializeResponseEntities(entities))
        .build();
  }

  private Map<String, Object> parseBody(DataFactoryConnectorResponse response) {
    try {
      return objectMapper.readerForMapOf(Object.class).readValue(response.getResponseBody());
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalArgumentException ("Couldn't deserialize response entity", ex);
    }
  }

  protected String serializeResponseEntities(List<Map<String, Object>> entities) {
    try {
      return this.objectMapper.writeValueAsString(entities);
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalArgumentException ("Couldn't serialize response", ex);
    }
  }
}
