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

package com.epam.digital.data.platform.bpm.it.config;

import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.model.CephObject;
import com.epam.digital.data.platform.integration.ceph.model.CephObjectMetadata;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TestCephServiceImpl implements CephService {

  private final Map<String, Object> storage = new HashMap<>();

  private String cephBucketName;
  private ObjectMapper objectMapper;

  @Override
  public Optional<String> getAsString(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    return Optional.ofNullable((String) storage.get(key));
  }

  @Override
  public Optional<CephObject> get(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    if (Objects.isNull(storage.get(key))) {
      return Optional.empty();
    }
    return Optional.of((CephObject) storage.get(key));
  }

  @Override
  public void put(String cephBucketName, String key, String content) {
    verifyBucketName(cephBucketName);
    storage.put(key, content);
  }

  @Override
  public CephObjectMetadata put(String cephBucketName, String key, String contentType,
      Map<String, String> userMetadata, InputStream fileInputStream) {
    verifyBucketName(cephBucketName);
    var metadata = CephObjectMetadata.builder()
        .userMetadata(userMetadata)
        .contentType(contentType)
        .contentLength(1000L)
        .build();
    var content = CephObject.builder()
        .content(fileInputStream)
        .metadata(metadata)
        .build();
    storage.put(key, content);
    return metadata;
  }

  @Override
  public void delete(String cephBucketName, Set<String> keys) {
    verifyBucketName(cephBucketName);
    var systemSignaturePrefix = "lowcode_";
    keys.stream().filter(k -> !k.startsWith(systemSignaturePrefix)).peek(storage::remove);
  }

  @Override
  public Boolean exist(String cephBucketName, Set<String> keys) {
    verifyBucketName(cephBucketName);
    return keys.stream().allMatch(storage::containsKey);
  }

  @Override
  public Boolean exist(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    return storage.containsKey(key);
  }

  @Override
  public Set<String> getKeys(String cephBucketName, String prefix) {
    return storage.keySet().stream()
        .filter(s -> s.startsWith(prefix))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> getKeys(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<CephObjectMetadata> getMetadata(String cephBucketName, Set<String> keys) {
    boolean allContains = keys.stream().allMatch(storage::containsKey);
    if (!allContains) {
      return Collections.emptyList();
    }
    return keys.stream().map(k -> ((CephObject) storage.get(k)).getMetadata())
        .collect(Collectors.toList());
  }

  public void clearStorage() {
    storage.clear();
  }

  private void verifyBucketName(String bucketName) {
    if (!cephBucketName.equals(bucketName)) {
      throw new MisconfigurationException("Bucket " + bucketName + " hasn't found");
    }
  }
}