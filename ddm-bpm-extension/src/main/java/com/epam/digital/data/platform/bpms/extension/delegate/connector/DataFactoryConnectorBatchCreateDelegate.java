package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(DataFactoryConnectorBatchCreateDelegate.DELEGATE_NAME)
public class DataFactoryConnectorBatchCreateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorBatchCreateDelegate";

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
  public void executeInternal(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var resource = resourceVariable.from(execution).get();
    var payload = payloadVariable.from(execution).getOrDefault(Spin.JSON(Map.of()));

    logProcessExecution("batch create entities on resource", resource);
    var response = executeBatchCreateOperation(execution, payload, resource);

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse executeBatchCreateOperation(DelegateExecution execution,
      SpinJsonNode payload, String resource) throws Exception {
    var spinJsonNodes = payload.elements();
    for (int nodeIndex = 0; nodeIndex < spinJsonNodes.size(); nodeIndex++) {
      var spinJsonNode = spinJsonNodes.get(nodeIndex);
      var stringJsonNode = spinJsonNode.toString();

      var systemSignature = signNode(execution, stringJsonNode);

      logProcessExecution("put signature to ceph", String.valueOf(nodeIndex));
      putSignatureToCeph(execution, stringJsonNode, systemSignature, nodeIndex);

      logProcessExecution("create entity", String.valueOf(nodeIndex));
      performPost(execution, resource, stringJsonNode);
    }
    return ConnectorResponse.builder()
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
    xDigitalSignatureDerivedCephKeyVariable.on(execution).set(systemSignatureCephKey);
  }

  private Object signNode(DelegateExecution execution, String stringJsonNode) throws Exception {
    var signRequestMap = Map.of("data", stringJsonNode);
    var signRequestPayload = Spin.JSON(signRequestMap);

    payloadVariable.on(execution).remove();
    payloadVariable.on(execution).set(signRequestPayload);
    digitalSignatureConnectorDelegate.execute(execution);
    var responseVariable = digitalSignatureConnectorDelegate.getDsoResponseVariable();
    var stringSystemSignature = responseVariable.from(execution).get();
    responseVariable.on(execution).remove();
    return Spin.JSON(stringSystemSignature).prop("signature").value();
  }

  private void performPost(DelegateExecution delegateExecution, String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
