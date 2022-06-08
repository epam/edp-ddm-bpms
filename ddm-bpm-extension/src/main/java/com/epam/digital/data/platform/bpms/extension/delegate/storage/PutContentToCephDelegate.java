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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to put data in ceph
 * as string using {@link CephService} service.
 */
@Slf4j
@Component(PutContentToCephDelegate.DELEGATE_NAME)
public class PutContentToCephDelegate extends BaseCephDelegate {

  public static final String DELEGATE_NAME = "putContentToCephDelegate";
  public static final String PROP_DATA = "data";
  public static final String PROP_SIGNATURE = "signature";

  private final FormDataStorageService formDataStorageService;
  private final ObjectMapper objectMapper;

  public PutContentToCephDelegate(@Value("${ceph.bucket}") String cephBucketName,
      CephService cephService, FormDataStorageService formDataStorageService,
      ObjectMapper objectMapper) {
    super(cephBucketName, cephService);
    this.formDataStorageService = formDataStorageService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var key = keyVariable.from(execution).get();
    var content = contentVariable.from(execution).get();

    log.debug("Start putting content with key {}", key);
    formDataStorageService.putFormData(key, deserializeContent(content));
    log.debug("Content put successfully with key {}, bucket name {}", key, cephBucketName);
  }

  private FormDataDto deserializeContent(String content) {
    var jsonContent = Spin.JSON(content);
    return FormDataDto.builder()
        .signature(jsonContent.hasProp(PROP_SIGNATURE) ? jsonContent.prop(PROP_SIGNATURE)
            .stringValue() : null)
        .data(deserializeData(jsonContent))
        .build();
  }

  private LinkedHashMap<String, Object> deserializeData(SpinJsonNode content) {
    try {
      var data = content.prop(PROP_DATA).isString() ? content.prop(PROP_DATA).stringValue()
          : content.prop(PROP_DATA).toString();
      return objectMapper.readValue(data, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't deserialize content", e);
    }
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
