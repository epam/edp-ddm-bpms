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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used for digital
 * signature of data
 */
@Slf4j
@Component(DigitalSignatureConnectorDelegate.DELEGATE_NAME)
public class DigitalSignatureConnectorDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "digitalSignatureConnectorDelegate";

  private final String dsoBaseUrl;

  @Getter
  @SystemVariable(name = "response", isTransient = true)
  private NamedVariableAccessor<SpinJsonNode> dsoResponseVariable;

  @Autowired
  public DigitalSignatureConnectorDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${dso.url}") String dsoBaseUrl) {
    super(restTemplate, springAppName);
    this.dsoBaseUrl = dsoBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var payload = payloadVariable.from(execution).getOptional();

    log.debug("Start sending data to sign");
    var response = performPost(execution, payload.map(Objects::toString).orElse(null));
    log.debug("Got digital signature");

    dsoResponseVariable.on(execution).set(response.getResponseBody());
  }

  private ConnectorResponse performPost(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dsoBaseUrl).pathSegment("api", "eseal", "sign")
        .build().toUri();
    return perform(RequestEntity.post(uri).headers(getHeadersForSign(execution)).body(body));
  }

  private HttpHeaders getHeadersForSign(DelegateExecution execution) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    getAccessToken(execution)
        .ifPresent(xAccessToken -> headers.add("X-Access-Token", xAccessToken));
    return headers;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
