package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Test;
import ua.gov.mdtu.ddm.general.errorhandling.exception.SystemException;

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