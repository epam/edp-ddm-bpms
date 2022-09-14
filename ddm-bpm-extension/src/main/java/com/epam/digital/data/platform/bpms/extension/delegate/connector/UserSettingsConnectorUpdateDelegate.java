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
import com.epam.digital.data.platform.settings.model.dto.SettingsDeactivateChannelInputDto;
import com.epam.digital.data.platform.settings.model.dto.SettingsEmailInputDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to create or
 * update user settings.
 * @deprecated - Should not be used as settings must be changed only via citizen portal,
 * will be removed in future releases
 */
@Slf4j
@RequiredArgsConstructor
@Component(UserSettingsConnectorUpdateDelegate.DELEGATE_NAME)
@Deprecated(forRemoval = true)
public class UserSettingsConnectorUpdateDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "userSettingsConnectorUpdateDelegate";

  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  private final UserSettingsFeignClient userSettingsFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var payload = payloadVariable.from(execution).getOptional();

    log.debug("Start creating or updating user settings");
    var requestBodyOpt = payload.map(node -> node.mapTo(DelegateInputDto.class));

    var headers = headerBuilderFactory.builder()
            .contentTypeJson()
            .accessTokenHeader()
            .csrfProtectionHeaders()
            .build();
    var settingsEmailDto =
        requestBodyOpt
            .map(
                input -> {
                  var emailInputDto = new SettingsEmailInputDto();
                  emailInputDto.setAddress(input.getEmail());
                  return emailInputDto;
                })
            .orElse(null);

    // create channel info
    userSettingsFeignClient.activateEmailChannel(settingsEmailDto, headers);
    log.debug("Email channel info is created");
    // deactivate channel
    var deactivationDto = new SettingsDeactivateChannelInputDto();
    deactivationDto.setDeactivationReason("USER_DEACTIVATED_FROM_BP");
    userSettingsFeignClient.deactivateChannel(Channel.EMAIL.getValue(), deactivationDto, headers);
    log.debug("Email channel is deactivated");

    // get user settings id
    var settingsResponse = userSettingsFeignClient.performGet(headers);
    log.debug("User settings successfully created or updated");

    var connectorResponseBodyDto = DelegateOutputDto.builder()
            .settingsId(settingsResponse.getSettingsId())
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
  @Data
  @ToString
  private static class DelegateInputDto {
    @NotNull
    @JsonProperty("e-mail")
    private String email;

    private String phone;

    @JsonProperty("communicationIsAllowed")
    private boolean communicationAllowed;
  }

  @Builder
  @Getter
  @ToString
  private static class DelegateOutputDto {
    @JsonProperty("settings_id")
    private UUID settingsId;
  }
}
