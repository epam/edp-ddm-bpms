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

package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.notification.SendUserNotificationDelegate;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.spin.json.SpinJsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SendUserNotificationDelegateTest {

  @InjectMocks
  private SendUserNotificationDelegate sendUserNotificationDelegate;

  @Mock
  private ExecutionEntity execution;

  @Mock
  private NamedVariableAccessor<String> recipientVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> recipientVariableReadAccessor;

  @Mock
  private NamedVariableAccessor<String> subjectVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> subjectVariableReadAccessor;

  @Mock
  private NamedVariableAccessor<String> templateVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> templateVariableReadAccessor;

  @Mock
  private NamedVariableAccessor<SpinJsonNode> templateModelVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<SpinJsonNode> templateModelVariableReadAccessor;

  @Mock
  private UserNotificationFacade notificationFacade;

  @Mock
  private ProcessDefinitionEntity processDefinitionEntity;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(sendUserNotificationDelegate, "notificationRecipientVariable",
        recipientVariableAccessor);
    ReflectionTestUtils.setField(sendUserNotificationDelegate, "notificationSubjectVariable",
        subjectVariableAccessor);
    ReflectionTestUtils.setField(sendUserNotificationDelegate, "notificationTemplateVariable",
        templateVariableAccessor);
    ReflectionTestUtils.setField(sendUserNotificationDelegate, "notificationTemplateModelVariable",
        templateModelVariableAccessor);

    when(recipientVariableAccessor.from(execution)).thenReturn(recipientVariableReadAccessor);
    when(subjectVariableAccessor.from(execution)).thenReturn(subjectVariableReadAccessor);
    when(templateVariableAccessor.from(execution)).thenReturn(templateVariableReadAccessor);
    when(templateModelVariableAccessor.from(execution)).thenReturn(
        templateModelVariableReadAccessor);
  }

  @Test
  void shouldSendNotification() throws Exception {
    when(recipientVariableReadAccessor.get()).thenReturn("recipient");
    when(subjectVariableReadAccessor.get()).thenReturn("subject");
    when(templateVariableReadAccessor.get()).thenReturn("template");
    when(templateModelVariableReadAccessor.get()).thenReturn(
        SpinJsonNode.S("{\"key\": \"value\"}"));

    when(execution.getProcessDefinition()).thenReturn(processDefinitionEntity);
    when(processDefinitionEntity.getKey()).thenReturn("add-lab");
    when(execution.getCurrentActivityId()).thenReturn("Activity_1");
    when(execution.getActivityInstanceId()).thenReturn("e2503352-bcb2-11ec-b217-0a580a831053");
    when(execution.getProcessInstanceId()).thenReturn("e2503352-bcb2-11ec-b217-0a580a831054");
    when(execution.getProcessDefinitionId()).thenReturn("add-lab:5:ac2dfa60-bbe2-11ec-8421-0a58");

    sendUserNotificationDelegate.execute(execution);

    var notificationRecordDto = UserNotificationMessageDto.builder()
        .recipients(List.of(Recipient.builder()
            .parameters(Map.of("key", "value"))
            .id("recipient")
            .build()))
        .context(NotificationContextDto.builder()
            .system("Low-code Platform")
            .businessActivity("Activity_1")
            .businessActivityInstanceId("e2503352-bcb2-11ec-b217-0a580a831053")
            .businessProcessInstanceId("e2503352-bcb2-11ec-b217-0a580a831054")
            .businessProcess("add-lab")
            .businessProcessDefinitionId("add-lab:5:ac2dfa60-bbe2-11ec-8421-0a58")
            .build())
        .notification(UserNotificationDto.builder()
            .ignoreChannelPreferences(false)
            .templateName("template")
            .title("subject")
            .build())
        .build();

    verify(notificationFacade).sendNotification(notificationRecordDto);
  }
}
