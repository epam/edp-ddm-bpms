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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class ExternalSystemConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("external-system")
  private WireMockServer externalSystem1;

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorNoSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_no_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable systemName not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorNoMethodNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_no_method_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable methodName not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorWrongSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_wrong_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("External-system with name system not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorWrongMethodNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("external_system_connector_wrong_method_name_defined"));
    assertThat(ex.getMessage()).isEqualTo(
        "Method method in external-system system1 not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemConnectorMethod1() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Basic dXNlcjpwYXNz"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_connector_method_1");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testExternalSystemConnectorDelegate.bpmn")
  public void externalSystemPartnerTokenAuth() {
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/auth/partner/token"))
            .willReturn(aResponse().withStatus(200).withBody("{\"token\":\"bearer-token\"}"))));
    externalSystem1.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/get"))
            .withHeader("Authorization", equalTo("Bearer bearer-token"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("external_system_partner_token_auth");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
