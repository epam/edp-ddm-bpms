package ua.gov.mdtu.ddm.lowcode.bpms.it.builder;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StubData {

  private final String resource;
  private final String resourceId;
  private final String requestBody;
  @Builder.Default
  private final Map<String, String> queryParams = new HashMap<>();
  @Builder.Default
  private final Map<String, String> headers = new HashMap<>();
  private final String response;

}
