package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import ua.gov.mdtu.ddm.general.errorhandling.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.general.errorhandling.exception.ValidationException;

public class UserDataValidationExceptionMapperTest {

  @Test
  public void testUserDataValidationExceptionMapper() {
    var errorDto = new ValidationErrorDto();
    errorDto.setMessage("test msg");
    var ex = new ValidationException(errorDto);

    Response response = new UserDataValidationExceptionMapper().toResponse(ex);

    assertThat(response.getMediaType()).hasToString(MediaType.APPLICATION_JSON);
    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.getEntity()).isEqualTo(ex);
  }
}