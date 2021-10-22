package com.epam.digital.data.platform.bpms.extension.it;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.extension.it.builder.StubData;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.Spin;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

public class SettingsDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/connector/testGetSettings.bpmn"})
  public void shouldGetSettings() {
    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .resource("settings")
        .response("/json/getSettingsResponse.json")
        .headers(Map.of(
            PlatformHttpHeader.X_ACCESS_TOKEN.getName(), "token"))
        .build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("get_settings_key");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();

    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testUpdateSettings.bpmn"})
  public void shouldUpdateSettings() {
    stubSettingsRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .resource("settings")
        .requestBody("/json/updateSettingsRequestBody.json")
        .response("/json/updateSettingsResponse.json")
        .headers(Map.of(
            PlatformHttpHeader.X_ACCESS_TOKEN.getName(), "token"))
        .build());

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
