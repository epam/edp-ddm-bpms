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

package com.epam.digital.data.platform.bpms.extension.delegate.notification;

import com.epam.digital.data.platform.bpms.extension.config.BlackListEmailDomainsProperties;
import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SendUserNotificationByAddress extends BaseJavaDelegate {
  public static final String DELEGATE_NAME = "sendUserNotificationByAddressDelegate";
  private static final String NOTIFICATION_ADDRESS_VALIDATION_ERROR_CODE = "NOTIFICATION_ADDRESS_VALIDATION_ERROR";
  private static final String EMAIL_NOTIFICATION_REGEX = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.\\[^<>()\\[\\]\\\\.,;:\\s@\"\\]+)*)|(\".+\"))@(([[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z-0-9]+\\.)+[a-zA-Z]{2,}))$";

  private final UserNotificationFacade notificationFacade;
  private final BlackListEmailDomainsProperties blackListEmailDomainsProperties;

  @Value("${spring.application.name}")
  private String springAppName;

  @SystemVariable(name = "notificationSubject", isTransient = true)
  protected NamedVariableAccessor<String> notificationSubjectVariable;
  @SystemVariable(name = "notificationTemplate", isTransient = true)
  protected NamedVariableAccessor<String> notificationTemplateVariable;
  @SystemVariable(name = "notificationChannel", isTransient = true)
  protected NamedVariableAccessor<String> notificationChannelVariable;
  @SystemVariable(name = "notificationAddress", isTransient = true)
  protected NamedVariableAccessor<String> notificationAddressVariable;
  @SystemVariable(name = "notificationTemplateModel", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> notificationTemplateModelVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var notificationChannel = notificationChannelVariable.from(execution).getOrThrow();
    var notificationAddress = notificationAddressVariable.from(execution).getOrThrow();
    if (Channel.EMAIL.name().equals(notificationChannel)) {
      validateEmail(notificationAddress);
    }
    var notificationSubject = notificationSubjectVariable.from(execution).get();
    var notificationTemplate = notificationTemplateVariable.from(execution).get();
    var notificationTemplateModel = (Map<String, Object>) notificationTemplateModelVariable.from(execution)
            .getOrThrow().mapTo(Map.class);
    var notificationMessage = UserNotificationMessageDto.builder()
            .recipients(List.of(Recipient.builder()
                    .channels(List.of(ChannelObject.builder()
                            .email(notificationAddress)
                            .channel(notificationChannel.toLowerCase())
                            .build()))
                    .parameters(notificationTemplateModel)
                    .build()))
            .context(NotificationContextDto.builder()
                    .system("Low-code Platform")
                    .application(springAppName)
                    .businessActivity(execution.getCurrentActivityId())
                    .businessActivityInstanceId(execution.getActivityInstanceId())
                    .businessProcessInstanceId(execution.getProcessInstanceId())
                    .businessProcess(((ExecutionEntity) execution).getProcessDefinition().getKey())
                    .businessProcessDefinitionId(execution.getProcessDefinitionId())
                    .build())
            .notification(UserNotificationDto.builder()
                    .ignoreChannelPreferences(true)
                    .templateName(notificationTemplate)
                    .title(notificationSubject)
                    .build())
            .build();

    notificationFacade.sendNotification(notificationMessage);
  }

  private void validateEmail(String email) {
    var domains = blackListEmailDomainsProperties.getDomains();
    if (!email.matches(EMAIL_NOTIFICATION_REGEX) || domains.stream().anyMatch(email::contains)) { //NOSONAR
      log.info("Email '{}' matched with black list", email);
      throw new BpmnError(NOTIFICATION_ADDRESS_VALIDATION_ERROR_CODE, String.format("This email %s didn't pass validation", email));
    }
  }
}
