/*
 * Copyright 2023 EPAM Systems.
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
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.extension.it.util.TestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class TrembitaSoapConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("soap-connector")
  protected WireMockServer soapHttpConnectorMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn"})
  public void shouldPassWithoutErrorsXmlResponse() throws Exception {
    var response = Files.readString(
        Paths.get(TestUtils.class.getResource("/xml/trembitaSoapConnectorResponse.xml").toURI()),
        StandardCharsets.UTF_8);
    stubSoapHttpConnector(response);

    var processInstance = runtimeService
        .startProcessInstanceByKey("trembita-soap-delegate");

    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testTrembitaSoapConnectorDelegateStringResponse.bpmn"})
  public void shouldPassWithoutErrorsStringResponse() throws Exception {
    var response = "Could not find addresses for service provider";
    stubSoapHttpConnector(response);

    var processInstance = runtimeService
        .startProcessInstanceByKey("trembita-soap-delegate-string-response");

    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = "bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn")
  public void trembitaSoapConnectorNoSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("trembita_soap_connector_no_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable systemName not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn")
  public void trembitaSoapConnectorNoPayloadDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("trembita_soap_connector_no_payload_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable payload not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn")
  public void trembitaSoapConnectorNoTrembitaSoapActionDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("trembita_soap_connector_no_trembita_soap_action_defined"));
    assertThat(ex.getMessage()).isEqualTo("Variable trembitaSoapAction not found");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn")
  public void trembitaSoapConnectorWrongSystemNameDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("trembita_soap_connector_wrong_system_name_defined"));
    assertThat(ex.getMessage()).isEqualTo(
        "Trembita system configuration with name system not configured");
  }

  @Test
  @Deployment(resources = "bpmn/connector/testTrembitaSoapConnectorDelegate.bpmn")
  public void trembitaSoapConnectorNoSubsystemPropertiesDefined() {
    var ex = assertThrows(IllegalArgumentException.class, () -> runtimeService
        .startProcessInstanceByKey("trembita_soap_connector_no_subsystem_properties_defined"));
    assertThat(ex.getMessage()).isEqualTo(
        "Trembita system configuration with name system-without-subsystem-properties not configured");
  }


  private void stubSoapHttpConnector(String response) throws Exception {
    var systemHeaders = Files.readString(
        Paths.get(TestUtils.class.getResource("/xml/trembitaSystemHeadersRequest.xml").toURI()),
        StandardCharsets.UTF_8);
    soapHttpConnectorMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/soap-connector-mock-server"))
            .withHeader("Content-Type", equalTo("text/xml;charset=UTF-8;"))
            .withHeader("SOAPAction", equalTo("action"))
            .withRequestBody(matching(String.format(".*%s.*", systemHeaders)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }
}