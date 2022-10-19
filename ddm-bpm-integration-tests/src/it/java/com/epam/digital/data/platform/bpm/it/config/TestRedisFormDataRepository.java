/*
 * Copyright 2022 EPAM Systems.
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

import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.repository.FormDataRepository;
import java.util.HashMap;
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
public class TestRedisFormDataRepository implements FormDataRepository {

  private final Map<String, Object> storage = new HashMap<>();

  @Override
  public Optional<FormDataDto> getFormData(String key) {
    if (Objects.isNull(storage.get(key))) {
      return Optional.empty();
    }
    return Optional.of((FormDataDto) storage.get(key));
  }

  @Override
  public void putFormData(String key, FormDataDto formDataDto) {
    storage.put(key, formDataDto);
  }

  @Override
  public Set<String> getKeys(String prefix) {
    return storage.keySet().stream()
        .filter(s -> s.startsWith(prefix))
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Set<String> keys) {
    var systemSignaturePrefix = "lowcode_";
    keys.stream().filter(k -> !k.startsWith(systemSignaturePrefix)).peek(storage::remove);
  }

  @Override
  public Set<String> keys() {
    throw new UnsupportedOperationException();
  }

  public void clearStorage() {
    storage.clear();
  }
}
