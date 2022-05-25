/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class DataFactoryConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("dataFactoryMockServer")
  private WireMockServer dataFactoryMockServer;
  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key");
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testNotFoundDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(404)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"NOT_FOUND\"}"))));

    var ex = assertThrows(ValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("NOT_FOUND");
    assertThat(ex.getMessage()).isNull();
    assertThat(ex.getDetails()).isNotNull();
    assertThat(ex.getDetails().getErrors()).hasSize(1);
    assertThat(ex.getDetails().getErrors().get(0).getMessage()).isEqualTo("Ресурс не знайдено");
    assertThat(ex.getDetails().getErrors().get(0).getField()).isNull();
    assertThat(ex.getDetails().getErrors().get(0).getValue()).isNull();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testValidationErrorDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"VALIDATION_ERROR\","
                    + "\"message\":\"Validation error\","
                    + "\"details\":{\"errors\":[{\"field\":\"field1\",\"value\":\"value1\","
                    + "\"message\":\"message1\"}]}}"))));

    var ex = assertThrows(ValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(ex.getMessage()).isEqualTo("Validation error");
    assertThat(ex.getDetails()).isNotNull();
    assertThat(ex.getDetails().getErrors()).hasSize(1);
    assertThat(ex.getDetails().getErrors().get(0).getMessage())
        .isEqualTo("Значення змінної не відповідає правилам вказаним в домені");
    assertThat(ex.getDetails().getErrors().get(0).getField()).isEqualTo("field1");
    assertThat(ex.getDetails().getErrors().get(0).getValue()).isEqualTo("value1");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testServerErrorDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(409)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"CONSTRAINT_VIOLATION\","
                    + "\"message\":\"System Error\"}"))));

    var ex = assertThrows(SystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("CONSTRAINT_VIOLATION");
    assertThat(ex.getMessage()).isEqualTo("System Error");
    assertThat(ex.getLocalizedMessage()).isEqualTo("Порушення одного з обмежень на рівні БД");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorDeleteDelegate.bpmn"})
  public void testDataFactoryConnectorDeleteDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(400)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"HEADERS_ARE_MISSING\"}"))));

    var ex = assertThrows(SystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorDeleteDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("HEADERS_ARE_MISSING");
    assertThat(ex.getMessage()).isNull();
    assertThat(ex.getLocalizedMessage()).isEqualTo("Відсутній обов’язковий заголовок");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorUpdateDelegate.bpmn"})
  public void testDataFactoryConnectorUpdateDelegate() {
    formDataStorageService.putFormData("cephKey", FormDataDto.builder()
        .accessToken("token").build());

    dataFactoryMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .withHeader("X-Digital-Signature-Derived", equalTo("cephKey"))
            .withRequestBody(equalTo("{\"var\":\"value\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withStatus(422).withBody("{\"traceId\":\"traceId1\",\"code\":\"VALIDATION_ERROR\","
                    + "\"details\":{\"errors\":[{\"field\":\"field1\",\"value\":\"value1\","
                    + "\"message\":\"message1\"}]}}"))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");
    var ex = assertThrows(ValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorUpdateDelegate_key", variables));

    assertThat(ex).isNotNull();
    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(ex.getMessage()).isNull();
    assertThat(ex.getDetails()).isNotNull();
    assertThat(ex.getDetails().getErrors()).hasSize(1);
    assertThat(ex.getDetails().getErrors().get(0).getMessage())
        .isEqualTo("Значення змінної не відповідає правилам вказаним в домені");
    assertThat(ex.getDetails().getErrors().get(0).getField()).isEqualTo("field1");
    assertThat(ex.getDetails().getErrors().get(0).getValue()).isEqualTo("value1");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorPartialUpdateDelegate.bpmn"})
  public void testDataFactoryConnectorPartialUpdateDelegate() {
    formDataStorageService.putFormData("cephKey", FormDataDto.builder()
        .accessToken("token").build());

    dataFactoryMockServer.addStubMapping(
        stubFor(patch(urlPathEqualTo("/mock-server/partial/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .withHeader("X-Digital-Signature-Derived", equalTo("cephKey"))
            .withRequestBody(equalToJson("{\"var\":\"value\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withStatus(201))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");
    var processInstance = runtimeService.startProcessInstanceByKey(
        "testDataFactoryConnectorPartialUpdateDelegate_key", variables);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorDelegate.bpmn"})
  public void testDataFactoryConnectorDelegate_withPartialUpdate() {
    formDataStorageService.putFormData("cephKey", FormDataDto.builder()
        .accessToken("token").build());

    dataFactoryMockServer.addStubMapping(
        stubFor(patch(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .withHeader("X-Digital-Signature-Derived", equalTo("cephKey"))
            .withRequestBody(equalToJson("{\"var\":\"value\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withStatus(201))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");
    var processInstance = runtimeService.startProcessInstanceByKey(
        "testDataFactoryConnectorDelegate_key", variables);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorSearchDelegate.bpmn"})
  public void testDataFactoryConnectorSearchDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory"))
            .withQueryParam("id", equalTo("id1"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(200))));

    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlEqualTo("/mock-server/laboratory"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(412)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"SIGNATURE_VIOLATION\"}"))));

    var ex = assertThrows(SystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorSearchDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getCode()).isEqualTo("SIGNATURE_VIOLATION");
    assertThat(ex.getMessage()).isNull();
    assertThat(ex.getLocalizedMessage()).isEqualTo("Данні в тілі не відповідають підпису");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorBatchCreateDelegate.bpmn"})
  public void testDataFactoryConnectorBatchCreateDelegate() {
    digitalSignatureMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(equalTo(
                "{\"data\":\"{\\\"data\\\":\\\"test data\\\",\\\"description\\\":\\\"some description\\\"}\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                .withBody("{\"signature\":\"signature\"}"))));

    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/test"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withHeader("X-Digital-Signature",
                matching("process/.*/task/test_token"))
            .withHeader("X-Digital-Signature-Derived",
                matching("lowcode_.*_system_signature_ceph_key_0"))
            .withRequestBody(
                equalTo("{\"data\":\"test data\",\"description\":\"some description\"}"))
            .willReturn(aResponse().withStatus(201))));

    digitalSignatureMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(equalTo(
                "{\"data\":\"{\\\"data2\\\":\\\"test data2\\\",\\\"description2\\\":\\\"some description2\\\"}\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                .withBody("{\"signature\":\"signature2\"}"))));

    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/test"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withHeader("X-Digital-Signature",
                matching("process/.*/task/test_token"))
            .withHeader("X-Digital-Signature-Derived",
                matching("lowcode_.*_system_signature_ceph_key_1"))
            .withRequestBody(
                equalTo("{\"data2\":\"test data2\",\"description2\":\"some description2\"}"))
            .willReturn(aResponse().withStatus(201))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorBatchCreateDelegate_key");

    var cephKeyToken = cephKeyProvider
        .generateKey("test_token", processInstance.getProcessInstanceId());
    formDataStorageService.putFormData(cephKeyToken, FormDataDto.builder().accessToken(validAccessToken)
        .data(new LinkedHashMap<>()).build());

    var taskId = taskService.createTaskQuery().taskDefinitionKey("waitConditionTask").singleResult()
        .getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorBatchReadDelegate.bpmn"})
  public void testDataFactoryConnectorBatchReadDelegate() {
    String chemResearchId = "7074945f-e088-446b-8c28-325aca4f423f";
    String physResearchId = "0b3c9f55-ba50-4d87-970a-bfbb8e31adeb";

    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(String.format("/mock-server/research/%s", chemResearchId)))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(200)
                .withBody(convertJsonToString("/json/researchResponseChem.json")))));

    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(String.format("/mock-server/research/%s", physResearchId)))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("ddm-bpm-extension"))
            .willReturn(aResponse().withStatus(200)
                .withBody(convertJsonToString("/json/researchResponsePhys.json")))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("test-batch-read",
            Map.of("resourceIds", List.of(chemResearchId, physResearchId)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorCreateDelegate.bpmn"})
  public void testDataFactoryConnectorCreateDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/test"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(
                equalTo("{\"data\":\"test data\",\"description\":\"some description\"}"))
            .willReturn(aResponse().withStatus(201))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorCreateDelegate_key");

    var cephKeyToken = cephKeyProvider
        .generateKey("test_token", processInstance.getProcessInstanceId());
    formDataStorageService.putFormData(cephKeyToken,
        FormDataDto.builder().accessToken(validAccessToken).data(new LinkedHashMap<>()).build());

    var taskId = taskService.createTaskQuery().taskDefinitionKey("waitConditionCreateDelegateTask")
        .singleResult()
        .getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorNestedCreateDelegate.bpmn"})
  public void testDataFactoryConnectorNestedCreateDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/mock-server/nested/test"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(
                equalTo("{\"data\":\"test data\",\"description\":\"some description\"}"))
            .willReturn(aResponse().withStatus(201))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorNestedCreateDelegate_key");

    var cephKeyToken = cephKeyProvider
        .generateKey("test_token", processInstance.getProcessInstanceId());
    formDataStorageService.putFormData(cephKeyToken,
        FormDataDto.builder().accessToken(validAccessToken)
            .data(new LinkedHashMap<>()).build());

    var taskId = taskService.createTaskQuery().taskDefinitionKey("waitConditionCreateDelegateTask")
        .singleResult()
        .getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
