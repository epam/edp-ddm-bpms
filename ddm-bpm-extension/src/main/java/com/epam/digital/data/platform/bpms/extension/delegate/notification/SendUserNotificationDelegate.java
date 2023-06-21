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

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.Recipient.RecipientRealm;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class SendUserNotificationDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "sendUserNotificationDelegate";
  private final UserNotificationFacade notificationFacade;
  @SystemVariable(name = "notificationRecipient", isTransient = true)
  protected NamedVariableAccessor<String> notificationRecipientVariable;
  @SystemVariable(name = "notificationSubject", isTransient = true)
  protected NamedVariableAccessor<String> notificationSubjectVariable;
  @SystemVariable(name = "notificationTemplate", isTransient = true)
  protected NamedVariableAccessor<String> notificationTemplateVariable;
  @SystemVariable(name = "notificationTemplateModel", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> notificationTemplateModelVariable;
  @SystemVariable(name = "notificationRecipientRealm", isTransient = true)
  protected NamedVariableAccessor<String> notificationRecipientRealmVariable;
  @Value("${spring.application.name}")
  private String springAppName;

  @Override
  @SuppressWarnings("unchecked")
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var notificationRecipient = notificationRecipientVariable.from(execution).get();
    var notificationRecipientRealmFromTemplate = notificationRecipientRealmVariable.from(execution)
        .get();
    var notificationRecipientRealm =
        RecipientRealm.OFFICER.getName().equalsIgnoreCase(notificationRecipientRealmFromTemplate)
            ? RecipientRealm.OFFICER
            : RecipientRealm.CITIZEN;

    var notificationSubject = notificationSubjectVariable.from(execution).get();
    var notificationTemplate = notificationTemplateVariable.from(execution).get();
    var notificationTemplateModel = (Map<String, Object>) Objects.requireNonNull(
        notificationTemplateModelVariable.from(execution).get()).mapTo(Map.class);

    var notificationMessage = UserNotificationMessageDto.builder()
        .recipients(List.of(Recipient.builder()
            .parameters(notificationTemplateModel)
            .id(notificationRecipient)
            .realm(notificationRecipientRealm)
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
            .ignoreChannelPreferences(false)
            .templateName(notificationTemplate)
            .title(notificationSubject)
            .build())
        .build();

    notificationFacade.sendNotification(notificationMessage);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
