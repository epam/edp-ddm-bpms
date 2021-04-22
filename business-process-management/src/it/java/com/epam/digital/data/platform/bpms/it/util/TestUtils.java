package com.epam.digital.data.platform.bpms.it.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TestUtils {

  public static String getContent(String content) throws IOException {
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
