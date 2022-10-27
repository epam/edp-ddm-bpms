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

package com.epam.digital.data.platform.bpms.extension.it.config;

import com.epam.digital.data.platform.bpms.extension.delegate.notification.SendUserNotificationDelegate;
import com.epam.digital.data.platform.starter.notifications.facade.UserNotificationFacade;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestNotificationsStarterConfig {

    @Bean
    @Primary
    public UserNotificationFacade notificationFacade() {
        return Mockito.mock(UserNotificationFacade.class);
    }

    @Bean(name = SendUserNotificationDelegate.DELEGATE_NAME)
    public SendUserNotificationDelegate sendUserNotificationDelegate(
        UserNotificationFacade notificationFacade) {
        return new SendUserNotificationDelegate(notificationFacade);
    }
}
