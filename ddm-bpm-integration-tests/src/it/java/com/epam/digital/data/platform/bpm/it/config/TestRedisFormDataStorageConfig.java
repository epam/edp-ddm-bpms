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

import com.epam.digital.data.platform.storage.form.model.RedisKeysSearchParams;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.storage.form.service.RedisFormDataStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRedisFormDataStorageConfig {

  @Bean
  @ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "test-redis")
  public FormDataStorageService<RedisKeysSearchParams> redisFormDataStorageService(TestRedisFormDataRepository redisFormDataRepository) {
    return RedisFormDataStorageService.builder()
        .keyProvider(new FormDataKeyProviderImpl())
        .repository(redisFormDataRepository)
        .build();
  }

  @Bean
  public TestRedisFormDataRepository redisFormDataRepository() {
    return new TestRedisFormDataRepository();
  }
}
