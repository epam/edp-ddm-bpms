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

package com.epam.digital.data.platform.bpms.extension.delegate.storage;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get data from ceph
 * as string using {@link CephService} service.
 *
 * @deprecated because of form data storage migration from ceph to redis
 */
@Deprecated
@Slf4j
@Component(GetContentFromCephDelegate.DELEGATE_NAME)
public class GetContentFromCephDelegate extends BaseCephDelegate {

  public static final String DELEGATE_NAME = "getContentFromCephDelegate";
  private final FormDataStorageService formDataStorageService;
  private final ObjectMapper objectMapper;

  public GetContentFromCephDelegate(FormDataStorageService formDataStorageService,
      ObjectMapper objectMapper) {
    this.formDataStorageService = formDataStorageService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var key = keyVariable.from(execution).get();

    log.debug("Start getting content by key {}", key);
    var content = formDataStorageService.getFormData(key);
    log.debug("Got content by key {}", key);

    contentVariable.on(execution).set(serializeFormData(content.orElse(null)));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  private String serializeFormData(FormDataDto formDataDto) {
    try {
      return objectMapper.writeValueAsString(formDataDto);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't serialize content", e);
    }
  }
}
