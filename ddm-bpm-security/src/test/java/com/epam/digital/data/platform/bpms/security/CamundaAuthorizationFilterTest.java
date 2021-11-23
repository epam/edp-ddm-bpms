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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class CamundaAuthorizationFilterTest {

  @InjectMocks
  private CamundaAuthorizationFilter camundaAuthorizationFilter;
  @Mock
  private CamundaAuthProvider camundaAuthProvider;
  @Mock
  private HttpServletRequest httpServletRequest;
  @Mock
  private HttpServletResponse httpServletResponse;
  @Mock
  private FilterChain filterChain;

  @Test
  public void testAuthFilter() throws ServletException, IOException {
    var auth = new UsernamePasswordAuthenticationToken("user", "access_token");
    SecurityContextHolder.getContext().setAuthentication(auth);

    camundaAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse,
        filterChain);

    verify(camundaAuthProvider, times(1)).createAuthentication(auth);
    verify(camundaAuthProvider, times(1)).clearAuthentication();
  }
}