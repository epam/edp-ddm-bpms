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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
public class ExternalSystemConfigurationProperties {

  private String url;
  private String type;
  private String protocol;
  private Map<String, OperationConfiguration> operations;
  private AuthenticationConfiguration auth;

  @Getter
  @Setter
  public static class OperationConfiguration {

    private String resourcePath;
    private HttpMethod method;
  }

  @Getter
  @Setter
  public static class AuthenticationConfiguration {

    private AuthenticationType type;
    private Secret secret;
    private String authUrl;
    private String accessTokenJsonPath;

    public enum AuthenticationType {
      NO_AUTH("NO_AUTH"),
      BASIC("BASIC"),
      AUTH_TOKEN("AUTH_TOKEN"),
      AUTH_TOKEN_BEARER("AUTH_TOKEN+BEARER"),
      BEARER("BEARER");

      private static final Map<String, AuthenticationType> valueMap = Map.of(
              NO_AUTH.getCode(), NO_AUTH,
              BASIC.getCode(), BASIC,
              AUTH_TOKEN.getCode(), AUTH_TOKEN,
              AUTH_TOKEN_BEARER.getCode(), AUTH_TOKEN_BEARER,
              BEARER.getCode(), BEARER
      );

      private final String code;

      AuthenticationType(String code) {
        this.code = code;
      }

      @JsonValue
      public String getCode() {
        return code;
      }

      @JsonCreator
      public static AuthenticationType getValueByCode(String code) {
        return valueMap.get(code);
      }

    }

    @Getter
    @Setter
    public static class Secret {

      private String username;
      private String password;
      private String token;

    }
  }
}
