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

package com.epam.digital.data.platform.bpms.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.camunda.bpm.engine.IdentityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(MockitoJUnitRunner.class)
public class CamundaAuthProviderTest {

  @InjectMocks
  private CamundaAuthProvider camundaAuthProvider;
  @Mock
  private IdentityService identityService;

  @Test
  public void testCreateAuthentication() {
    var auth = new UsernamePasswordAuthenticationToken("user", "access_token",
        List.of(new SimpleGrantedAuthority("testRole")));

    camundaAuthProvider.createAuthentication(auth);

    verify(identityService, times(1)).setAuthentication("user", List.of("testRole"));
  }

  @Test
  public void shouldNotCreateAuthentication() {
    camundaAuthProvider.createAuthentication(null);

    verify(identityService, never()).setAuthentication(any(), any());
  }

  @Test
  public void shouldClearAuthentication() {
    camundaAuthProvider.clearAuthentication();

    verify(identityService, times(1)).clearAuthentication();
  }
}