package com.epam.digital.data.platform.bpms.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

public class CamundaRestExceptionMapperTest {

  private final CamundaRestExceptionMapper mapper = new CamundaRestExceptionMapper();

  @Test
  public void toResponseTest() {
    var traceId = "traceId";
    var code = RestException.class.getSimpleName();
    var message = "message";

    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, traceId);

    var response = mapper.toResponse(new RestException(Status.NOT_FOUND, "message"));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(response.getEntity()).isEqualTo(SystemErrorDto.builder()
        .traceId(traceId)
        .code(code)
        .message(message)
        .localizedMessage(message)
        .build());
  }

}
