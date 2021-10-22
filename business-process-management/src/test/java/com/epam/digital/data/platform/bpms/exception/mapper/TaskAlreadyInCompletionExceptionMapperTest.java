package com.epam.digital.data.platform.bpms.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class TaskAlreadyInCompletionExceptionMapperTest {

  @Mock
  private MessageResolver messageResolver;
  @InjectMocks
  private TaskAlreadyInCompletionExceptionMapper mapper;

  @Test
  public void toResponseTest() {
    var traceId = "traceId";
    var code = String.valueOf(HttpStatus.CONFLICT);
    var message = "message";
    var localizedMessage = "localizedMessage";

    when(messageResolver.getMessage("task.already.in.completion")).thenReturn(localizedMessage);

    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, traceId);

    var response = mapper.toResponse(new TaskAlreadyInCompletionException(message));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(response.getEntity()).isEqualTo(SystemErrorDto.builder()
        .traceId(traceId)
        .code(code)
        .message(message)
        .localizedMessage(localizedMessage)
        .build());
  }
}
