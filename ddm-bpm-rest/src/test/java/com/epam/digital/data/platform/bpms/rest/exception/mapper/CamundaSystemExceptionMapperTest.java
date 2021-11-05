package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;

public class CamundaSystemExceptionMapperTest {

  @Test
  public void testUserDataValidationExceptionMapper() {
    var ex = new SystemException(null);

    Response response = new CamundaSystemExceptionMapper().toResponse(ex);

    assertThat(response.getMediaType()).hasToString(MediaType.APPLICATION_JSON);
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(response.getEntity()).isSameAs(ex);
  }
}