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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;

public class SendUserNotificationDelegateIT extends BaseIT {

  @Autowired
  private UserNotificationFacade notificationFacade;

  @Captor
  private ArgumentCaptor<UserNotificationMessageDto> notificationRecordCaptor;

  @Test
  @Deployment(resources = "bpmn/delegate/sendUserNotificationDelegate.bpmn")
  public void sendNotification() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("sendUserNotificationDelegate_key");

    verify(notificationFacade).sendNotification(notificationRecordCaptor.capture());
    var actual = notificationRecordCaptor.getValue();
    assertThat(actual.getRecipients().get(0).getParameters()).isEqualTo(Map.of("name", "John"));
    assertThat(actual.getRecipients().get(0).getId()).isEqualTo("testuser");
    assertThat(actual.getContext().getSystem()).isEqualTo("Low-code Platform");
    assertThat(actual.getContext().getBusinessActivity()).isEqualTo("send_exerpt_notification");
    assertThat(actual.getContext().getBusinessProcessInstanceId()).isEqualTo(
        processInstance.getId());
    assertThat(actual.getContext().getBusinessProcessDefinitionId()).isEqualTo(
        processInstance.getProcessDefinitionId());
    assertThat(actual.getContext().getBusinessProcess()).isEqualTo(
        "sendUserNotificationDelegate_key");
    assertThat(actual.getNotification().getTemplateName()).isEqualTo("specific_exerpt_generated");
    assertThat(actual.getNotification().getTitle()).isEqualTo("Excerpt successfully generated");

    assertThat(processInstance.isEnded()).isTrue();
  }
}
