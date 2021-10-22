package com.epam.digital.data.platform.bpms.engine.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception that is thrown when the user task is completing and user at the moment tries to
 * complete it again (thrown for preventing double completion)
 */
@Getter
@RequiredArgsConstructor
public class TaskAlreadyInCompletionException extends RuntimeException {

  private final String message;
}
