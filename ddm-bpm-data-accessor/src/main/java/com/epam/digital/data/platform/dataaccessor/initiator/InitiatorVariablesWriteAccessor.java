package com.epam.digital.data.platform.dataaccessor.initiator;

import org.springframework.lang.Nullable;

/**
 * Class that is used for <i>initiator</i> variables write only accessing
 */
public interface InitiatorVariablesWriteAccessor {

  /**
   * Set process instance initiator token to a transient process variable
   *
   * @param accessToken token of the user that initiated the process instance
   */
  void setInitiatorAccessToken(@Nullable String accessToken);
}
