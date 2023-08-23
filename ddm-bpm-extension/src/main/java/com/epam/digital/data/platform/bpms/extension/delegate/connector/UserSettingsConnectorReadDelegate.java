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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.ChannelReadDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to read user
 * settings.
 */
@Slf4j
@RequiredArgsConstructor
@Component(UserSettingsConnectorReadDelegate.DELEGATE_NAME)
public class UserSettingsConnectorReadDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "userSettingsConnectorReadDelegate";

  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  private final UserSettingsFeignClient userSettingsFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    log.debug("Start reading user settings");
    responseVariable.on(execution).set(ConnectorResponse.builder().build());

    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .accessTokenHeader()
        .build();
    var settingsResponse = userSettingsFeignClient.performGet(headers);
    log.debug("User settings successfully read");

    var email = settingsResponse.getChannels().stream()
            .filter(channel -> Channel.EMAIL.equals(channel.getChannel()))
            .map(ChannelReadDto::getAddress)
            .findFirst()
            .orElse(null);

    var connectorResponseBodyDto = DelegateOutputDto.builder()
            .settingsId(settingsResponse.getSettingsId())
            .email(email)
            .build();

    var connectorResponse =
        ConnectorResponse.builder().responseBody(Spin.JSON(connectorResponseBodyDto)).build();
    responseVariable.on(execution).set(connectorResponse);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Builder
  @Getter
  @ToString
  private static class DelegateOutputDto {
    @JsonProperty("settings_id")
    private UUID settingsId;

    @JsonProperty("e-mail")
    private String email;

    private String phone;

    @JsonProperty("communicationIsAllowed")
    private boolean communicationAllowed;
  }

}
