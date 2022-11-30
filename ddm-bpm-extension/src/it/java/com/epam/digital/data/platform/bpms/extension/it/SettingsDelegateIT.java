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

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.Spin;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class SettingsDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/connector/testGetSettings.bpmn"})
  public void shouldGetSettings() {
    userSettingsWireMock.stubFor(
            get(urlPathEqualTo("/user-settings-mock-server/api/settings/me"))
                    .withHeader(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), equalTo("token"))
                    .willReturn(aResponse().withStatus(200)
                            .withHeader("Content-type", "application/json")
                            .withBody(convertJsonToString("/json/getSettingsResponse.json"))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("get_settings_key");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();

    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testUpdateSettings.bpmn"})
  public void shouldUpdateSettings() {
    userSettingsWireMock.stubFor(
        post(urlPathEqualTo("/user-settings-mock-server/api/settings/me/channels/email/deactivate"))
            .withHeader(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), equalTo("token"))
            .withRequestBody(
                new EqualToJsonPattern(
                    convertJsonToString("/json/updateSettingsDeactivate.json"), false, false))
            .willReturn(
                aResponse().withStatus(200).withHeader("Content-type", "application/json")));
    userSettingsWireMock.stubFor(
        get(urlPathEqualTo("/user-settings-mock-server/api/settings/me"))
            .withHeader(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), equalTo("token"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-type", "application/json")
                    .withBody(convertJsonToString("/json/getSettingsAfterDeactivateResponse.json"))));

    var processInstance = runtimeService
            .startProcessInstanceByKey("update_settings_key",
                    Map.of("dataPayload", Spin.JSON(Map.of(
                            "e-mail", "test@test.com",
                            "phone", "+380444444444",
                            "communicationIsAllowed", true))));

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstance.getId()).list();

    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
