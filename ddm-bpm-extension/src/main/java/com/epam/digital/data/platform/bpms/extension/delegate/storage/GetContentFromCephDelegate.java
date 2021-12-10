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
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get data from ceph
 * as string using {@link CephService} service.
 */
@Slf4j
@Component(GetContentFromCephDelegate.DELEGATE_NAME)
public class GetContentFromCephDelegate extends BaseCephDelegate {

  public static final String DELEGATE_NAME = "getContentFromCephDelegate";

  public GetContentFromCephDelegate(@Value("${ceph.bucket}") String cephBucketName,
      CephService cephService) {
    super(cephBucketName, cephService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var key = keyVariable.from(execution).get();

    log.debug("Start getting content by key {}", key);
    var content = cephService.getAsString(cephBucketName, key).orElse(null);
    log.debug("Got content by key {}", key);

    contentVariable.on(execution).set(content);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
