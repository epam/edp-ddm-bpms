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

package com.epam.digital.data.platform.bpm.it.util;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;

public final class TestUtils {

  @SneakyThrows
  public static String getContent(String content) {
    if (content.endsWith(".json")) {
      try {
        return Files.readString(
            Paths.get(TestUtils.class.getResource(content).toURI()), StandardCharsets.UTF_8);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    return content;
  }
}
