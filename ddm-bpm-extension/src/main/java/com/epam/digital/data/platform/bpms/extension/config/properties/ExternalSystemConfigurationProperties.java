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

package com.epam.digital.data.platform.bpms.extension.config.properties;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
public class ExternalSystemConfigurationProperties {

  private String url;
  private Map<String, MethodConfiguration> methods;
  private AuthenticationConfiguration auth;

  @Getter
  @Setter
  public static class MethodConfiguration {

    private String path;
    private HttpMethod method;
  }

  @Getter
  @Setter
  public static class AuthenticationConfiguration {

    private AuthenticationType type;
    private String secretName;
    private String partnerTokenAuthUrl;
    private String tokenJsonPath;

    public enum AuthenticationType {
      BASIC, PARTNER_TOKEN
    }
  }
}
