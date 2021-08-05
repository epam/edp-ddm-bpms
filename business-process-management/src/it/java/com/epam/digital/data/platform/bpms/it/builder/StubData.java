package com.epam.digital.data.platform.bpms.it.builder;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Builder
public class StubData {

  private HttpMethod httpMethod;
  private String resource;
  private String resourceId;
  private String requestBody;
  @Builder.Default
  private Map<String, String> queryParams = new HashMap<>();
  @Builder.Default
  private Map<String, String> headers = new HashMap<>();
  private String response;
  private UriComponentsBuilder uri;

}
