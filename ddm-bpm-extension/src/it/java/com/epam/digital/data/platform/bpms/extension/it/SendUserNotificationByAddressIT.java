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

import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class SendUserNotificationByAddressIT extends BaseIT {

  @Autowired
  @Qualifier("notificationFacadeByAddress")
  private UserNotificationFacade notificationFacadeByAddress;

  @Captor
  private ArgumentCaptor<UserNotificationMessageDto> notificationRecordCaptor;

  @Test
  @Deployment(resources = "bpmn/delegate/sendUserNotificationByAddressDelegate.bpmn")
  public void sendNotification() {
    var processInstance = runtimeService
            .startProcessInstanceByKey("sendUserNotificationByAddressDelegate_key");

    verify(notificationFacadeByAddress).sendNotification(notificationRecordCaptor.capture());
    var actual = notificationRecordCaptor.getValue();
    assertThat(actual.getRecipients().get(0).getParameters()).isEqualTo(Map.of("name", "John"));
    assertThat(actual.getRecipients().get(0).getChannels().get(0).getChannel()).isEqualTo(Channel.EMAIL.getValue());
    assertThat(actual.getRecipients().get(0).getChannels().get(0).getEmail()).isEqualTo("user@gmail.com");
    assertThat(actual.getContext().getSystem()).isEqualTo("Low-code Platform");
    assertThat(actual.getContext().getBusinessActivity()).isEqualTo("send_exerpt_notification");
    assertThat(actual.getContext().getBusinessProcessInstanceId()).isEqualTo(
            processInstance.getId());
    assertThat(actual.getContext().getBusinessProcessDefinitionId()).isEqualTo(
            processInstance.getProcessDefinitionId());
    assertThat(actual.getContext().getBusinessProcess()).isEqualTo(
            "sendUserNotificationByAddressDelegate_key");
    assertThat(actual.getNotification().getTemplateName()).isEqualTo("specific_exerpt_generated");
    assertThat(actual.getNotification().getTitle()).isEqualTo("Send User Notification By Address");

    assertThat(processInstance.isEnded()).isTrue();
  }
}
