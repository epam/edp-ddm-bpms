package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.service.MessageResolver;

@Component("dataFactoryConnectorBatchCreateDelegate")
@Logging
public class DataFactoryConnectorBatchCreateDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;
  private final DigitalSignatureConnectorDelegate digitalSignatureConnectorDelegate;
  private final CephService cephService;

  private final String cephBucketName;

  public DataFactoryConnectorBatchCreateDelegate(RestTemplate restTemplate, CephService cephService,
      ObjectMapper objectMapper, MessageResolver messageResolver,
      DigitalSignatureConnectorDelegate digitalSignatureConnectorDelegate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, cephService, objectMapper, messageResolver, springAppName,
        cephBucketName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
    this.digitalSignatureConnectorDelegate = digitalSignatureConnectorDelegate;
    this.cephService = cephService;
    this.cephBucketName = cephBucketName;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    var response = executeBatchCreateOperation(execution, payload, resource);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse executeBatchCreateOperation(DelegateExecution execution,
      SpinJsonNode payload, String resource) {

    var spinJsonNodes = payload.elements();
    for (int nodeIndex = 0; nodeIndex < spinJsonNodes.size(); nodeIndex++) {
      var spinJsonNode = spinJsonNodes.get(nodeIndex);
      var stringJsonNode = spinJsonNode.toString();

      var systemSignature = signNode(execution, stringJsonNode);

      putSignatureToCeph(execution, stringJsonNode, systemSignature, nodeIndex);

      performPost(execution, resource, stringJsonNode);
    }
    return DataFactoryConnectorResponse.builder()
        .statusCode(HttpStatus.CREATED.value())
        .build();
  }

  private void putSignatureToCeph(DelegateExecution execution, String stringJsonNode,
      Object systemSignature, int nodeIndex) {
    var cephSignatureMap = Map.of("data", stringJsonNode, "signature", systemSignature);
    var cephContent = Spin.JSON(cephSignatureMap).toString();

    var processInstanceId = execution.getProcessInstanceId();
    var systemSignatureCephKey =
        "lowcode_" + processInstanceId + "_system_signature_ceph_key_" + nodeIndex;
    cephService.putContent(cephBucketName, systemSignatureCephKey, cephContent);
    execution.setVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
  }

  private Object signNode(DelegateExecution execution, String stringJsonNode) {
    var signRequestMap = Map.of("data", stringJsonNode);
    var signRequestPayload = Spin.JSON(signRequestMap).toString();

    execution.removeVariable(PAYLOAD_VARIABLE);
    ((AbstractVariableScope) execution)
        .setVariableLocalTransient(PAYLOAD_VARIABLE, signRequestPayload);
    digitalSignatureConnectorDelegate.execute(execution);
    var stringSystemSignature = execution.getVariable(RESPONSE_VARIABLE);
    execution.removeVariable(RESPONSE_VARIABLE);
    return Spin.JSON(stringSystemSignature).prop("signature").value();
  }

  private void performPost(DelegateExecution delegateExecution, String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    var requestEntity = RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body);
    try {
      perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildUpdatableException(requestEntity, ex);
    }
  }
}
