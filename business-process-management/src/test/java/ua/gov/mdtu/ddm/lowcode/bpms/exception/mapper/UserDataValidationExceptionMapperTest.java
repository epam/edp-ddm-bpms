package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

public class UserDataValidationExceptionMapperTest {

  @Test
  public void testUserDataValidationExceptionMapper() {
    UserDataValidationErrorDto errorDto = new UserDataValidationErrorDto();
    errorDto.setMessage("test msg");
    UserDataValidationException ex = new UserDataValidationException(errorDto);

    Response response = new UserDataValidationExceptionMapper()
        .toResponse(ex);

    assertThat(response.getMediaType().toString()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.getEntity()).isEqualTo(errorDto);
  }
}