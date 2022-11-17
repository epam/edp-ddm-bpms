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

package com.epam.digital.data.platform.bpm.history.it;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.epam.digital.data.platform.bpms.extension.delegate.UserDataValidationErrorDelegate;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class BaseTestConfig {

  @Bean
  public CustomScopeConfigurer customScopeConfigurer() {
    var scopeConfigurer = new CustomScopeConfigurer();

    scopeConfigurer.addScope(WebApplicationContext.SCOPE_REQUEST, new SimpleThreadScope());

    return scopeConfigurer;
  }

  @Bean("camundaAdminImpersonation")
  public CamundaImpersonationFactory camundaImpersonationFactory() {
    var factory = mock(CamundaImpersonationFactory.class);

    var impersonation = mock(CamundaImpersonation.class);

    doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(impersonation).execute(any());

    doReturn(Optional.of(impersonation)).when(factory).getCamundaImpersonation();

    return factory;
  }

  @Bean
  public UserDataValidationErrorDelegate userDataValidationErrorDelegate() {
    return new UserDataValidationErrorDelegate(new ObjectMapper());
  }
}
