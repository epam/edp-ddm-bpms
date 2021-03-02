package ua.gov.mdtu.ddm.lowcode.bpms.it.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;

public final class TestUtils {

  public static final String VARIABLE_NAME = "secure-sys-var-ref-task-form-data-%s";
  public static final String VARIABLE_VALUE = "lowcode-%s-%s";

  public static String getContent(String content) throws IOException {
    if (content.endsWith(".json")) {
      return new String(ByteStreams.toByteArray(TestUtils.class.getResourceAsStream(content)));
    }
    return content;
  }
}
