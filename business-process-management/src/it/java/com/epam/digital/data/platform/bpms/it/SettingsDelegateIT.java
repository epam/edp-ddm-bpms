package com.epam.digital.data.platform.bpms.it;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.Spin;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

public class SettingsDelegateIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/testGetSettings.bpmn"})
  public void shouldGetSettings() throws IOException {
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
  @Deployment(resources = {"bpmn/testUpdateSettings.bpmn"})
  public void shouldUpdateSettings() throws IOException {
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
