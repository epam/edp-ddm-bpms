package ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of data factory errors.
 */
@Getter
@RequiredArgsConstructor
public enum DataFactoryError {
  CLIENT_ERROR("data-factory.error.client-error"),
  HEADERS_ARE_MISSING("data-factory.error.header-are-missing"),
  INVALID_HEADER_VALUE("data-factory.error.invalid-header-value"),

  AUTHENTICATION_FAILED("data-factory.error.authentication-failed"),

  JWT_EXPIRED("data-factory.error.jwt-expired"),

  NOT_FOUND("data-factory.error.not-found"),

  CONSTRAINT_VIOLATION("data-factory.error.constraint-violation"),

  SIGNATURE_VIOLATION("data-factory.error.signature-violation"),

  VALIDATION_ERROR("data-factory.error.validation-error"),

  RUNTIME_ERROR("data-factory.error.runtime-error");

  private final String titleKey;

  /**
   * Search data factory error by name
   *
   * @param name error name
   * @return {@link DataFactoryError} object
   */
  public static DataFactoryError fromNameOrDefaultRuntimeError(String name) {

    return Arrays.stream(values())
        .filter(dataFactoryError -> dataFactoryError.name().equals(name))
        .findFirst().orElse(RUNTIME_ERROR);
  }
}
