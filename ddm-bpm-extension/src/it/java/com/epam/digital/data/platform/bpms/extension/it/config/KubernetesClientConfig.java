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

import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.net.HttpURLConnection;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientConfig {

  @Bean(destroyMethod = "after")
  public KubernetesServer kubernetesServer() {
    var server = new KubernetesServer();
    server.before();

    var secret = new SecretBuilder()
        .withData(Map.of("username", "dXNlcg==", "password", "cGFzcw=="))
        .build();
    server.expect().get()
        .withPath("/api/v1/namespaces/current-namespace/secrets/secret1")
        .andReturn(HttpURLConnection.HTTP_OK, secret)
        .always();

    var secret2 = new SecretBuilder()
        .withData(Map.of("token", "dG9rZW4="))
        .build();
    server.expect().get()
        .withPath("/api/v1/namespaces/current-namespace/secrets/secret2")
        .andReturn(HttpURLConnection.HTTP_OK, secret2)
        .always();

    return server;
  }

  @Bean()
  public KubernetesClient kubernetesClient(KubernetesServer kubernetesServer) {
    return kubernetesServer.getClient();
  }
}
