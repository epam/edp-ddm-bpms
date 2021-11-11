package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class UserDataValidationExceptionMapperTest {

  @Test
  void testUserDataValidationExceptionMapper() {
    var errorDto = new ValidationErrorDto();
    errorDto.setMessage("test msg");
    var ex = new ValidationException(errorDto);

    Response response = new UserDataValidationExceptionMapper().toResponse(ex);

    assertThat(response.getMediaType()).hasToString(MediaType.APPLICATION_JSON);
    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.getEntity()).isEqualTo(ex);
  }
}