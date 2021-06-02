package com.epam.digital.data.platform.bpms.el.juel;

import com.epam.digital.data.platform.bpms.el.dto.UserDto;
import com.epam.digital.data.platform.bpms.listener.InitiatorTokenStartEventListener;
import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

/**
 * Class with JUEL function that resolves an initiator info object
 *
 * @see InitiatorJuelFunction#initiator() The function itself
 */
@Component
public class InitiatorJuelFunction extends AbstractApplicationContextAwareJuelFunction {

  private static final String JUEL_FUNCTION_NAME = "initiator";
  private static final String INITIATOR_OBJ_VAR_NAME = "initiator-juel-function-result-object";

  public InitiatorJuelFunction() {
    super(JUEL_FUNCTION_NAME);
  }

  /**
   * Static JUEL function that resolves an {@link UserDto} object of the business-process initiator
   * <p>
   * Checks if there already is an object with initiator info in Camunda execution context and
   * returns it if it exists or else reads an initiator userName and token (if it still exist) from
   * initiator variables, parses token claims and creates an {@link UserDto} object with all found
   * data
   *
   * @return initiator {@link UserDto} representation
   */
  public static UserDto initiator() {
    final var execution = getExecution();

    var storedObject = (UserDto) execution.getVariable(INITIATOR_OBJ_VAR_NAME);

    if (storedObject != null) {
      return storedObject;
    }

    var initiatorUserName = getInitiatorUserName(execution);
    var initiatorAccessToken = getInitiatorAccessToken(execution);
    var claims = initiatorAccessToken != null ? parseClaims(initiatorAccessToken) : null;

    var userDto = new UserDto(initiatorUserName, initiatorAccessToken, claims);
    execution.removeVariable(INITIATOR_OBJ_VAR_NAME);
    execution.setVariableLocalTransient(INITIATOR_OBJ_VAR_NAME, userDto);
    return userDto;
  }

  private static JwtClaimsDto parseClaims(String initiatorAccessToken) {
    final var tokenParser = getBean(TokenParser.class);
    return tokenParser.parseClaims(initiatorAccessToken);
  }

  private static String getInitiatorAccessToken(CoreExecution execution) {
    final var initiatorAccessTokenVarName = InitiatorTokenStartEventListener.INITIATOR_TOKEN_VAR_NAME;
    return (String) execution.getVariable(initiatorAccessTokenVarName);
  }

  private static String getInitiatorUserName(CoreExecution execution) {
    var executionEntity = (ExecutionEntity) execution;
    final var initiatorVarName = (String) executionEntity.getProcessDefinition()
        .getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
    return (String) execution.getVariable(initiatorVarName);
  }
}
