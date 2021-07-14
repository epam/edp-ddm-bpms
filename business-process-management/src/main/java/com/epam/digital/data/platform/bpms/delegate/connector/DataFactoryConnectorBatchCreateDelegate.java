package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to execute
 * data batch creation in Data Factory
 */
@Component("dataFactoryConnectorBatchCreateDelegate")
@Logging
public class DataFactoryConnectorBatchCreateDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;
  private final DigitalSignatureConnectorDelegate digitalSignatureConnectorDelegate;
  private final CephService cephService;

  private final String cephBucketName;

  public DataFactoryConnectorBatchCreateDelegate(RestTemplate restTemplate, CephService cephService,
      DigitalSignatureConnectorDelegate digitalSignatureConnectorDelegate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
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
    var signRequestPayload = Spin.JSON(signRequestMap);

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

    perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }
}
