package ua.gov.mdtu.ddm.lowcode.bpms.it.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TestUtils {

  public static final String VARIABLE_NAME = "secure-sys-var-ref-task-form-data-%s";
  public static final String VARIABLE_VALUE = "lowcode-%s-%s";

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

  public static String formDataVariableName(String taskDefinitionKey) {
    return String.format(VARIABLE_NAME, taskDefinitionKey);
  }

  public static String formDataVariableValue(String processInstanceId, String varName) {
    return String.format(VARIABLE_VALUE, processInstanceId, varName);
  }
}
