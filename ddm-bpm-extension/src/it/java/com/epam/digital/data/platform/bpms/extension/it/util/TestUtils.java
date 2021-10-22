package com.epam.digital.data.platform.bpms.extension.it.util;

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
