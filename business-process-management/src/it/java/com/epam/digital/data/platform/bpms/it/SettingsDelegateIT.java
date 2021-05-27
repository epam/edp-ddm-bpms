package com.epam.digital.data.platform.bpms.it;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.spin.Spin;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

public class SettingsDelegateIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/testGetSettings.bpmn"})
  public void shouldGetSettings() throws IOException {
    stubDataFactoryRead(StubData.builder()
        .resource("settings")
        .response("/json/getSettingsResponse.json")
        .build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("get_settings_key");
    Assertions.assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/testUpdateSettings.bpmn"})
  public void shouldUpdateSettings() throws IOException {
    stubDataFactoryUpdate(StubData.builder()
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
                "communicationIsAllowed", true)),
                "x_access_token_task_definition_key", "token_key"));

    var xAccessTokenCephKey = cephKeyProvider
        .generateKey("token_key", processInstance.getId());

    formDataCephService.putFormData(xAccessTokenCephKey, FormDataDto.builder()
        .accessToken("token").build());
    completeTask("Activity_0mmvw19", processInstance.getId(), "{}");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();

    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
