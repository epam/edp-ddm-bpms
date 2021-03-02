package ua.gov.mdtu.ddm.lowcode.bpms.it.builder;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StubData {

  private String resource;
  private String requestBody;
  @Builder.Default
  private Map<String, String> queryParams = new HashMap<>();
  @Builder.Default
  private Map<String, String> headers = new HashMap<>();
  private String response;

}
