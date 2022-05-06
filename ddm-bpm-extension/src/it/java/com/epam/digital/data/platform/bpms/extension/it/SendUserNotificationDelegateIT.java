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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.dto.NotificationRecordDto;
import com.epam.digital.data.platform.starter.notifications.facade.NotificationFacade;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;

public class SendUserNotificationDelegateIT extends BaseIT {

  @Autowired
  private NotificationFacade notificationFacade;

  @Captor
  private ArgumentCaptor<NotificationRecordDto> notificationRecordCaptor;

  @Test
  @Deployment(resources = "bpmn/delegate/sendUserNotificationDelegate.bpmn")
  public void sendNotification() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("sendUserNotificationDelegate_key");

    verify(notificationFacade).sendNotification(notificationRecordCaptor.capture());
    assertEquals(createNotification(), notificationRecordCaptor.getValue().getNotification());
    assertThat(processInstance.isEnded()).isTrue();
  }

  private NotificationDto createNotification() {
    return NotificationDto.builder()
        .templateModel(Map.of("name", "John"))
        .subject("Excerpt successfully generated")
        .template("specific_exerpt_generated")
        .build();
  }
}
