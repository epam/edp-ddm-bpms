package com.epam.digital.data.platform.dataaccessor.initiator;

import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * Class that is used for <i>initiator</i> variables read only accessing
 */
public interface InitiatorVariablesReadAccessor {

  /**
   * Get process instance initiator from a process variable
   *
   * @return name of the user that initiated the process instance
   */
  @NonNull
  Optional<String> getInitiatorName();

  /**
   * Get initiator token from a process variable
   *
   * @return token of the user that initiated the process instance
   */
  @NonNull
  Optional<String> getInitiatorAccessToken();
}
