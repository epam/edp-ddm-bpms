package com.epam.digital.data.platform.bpms.el.juel.mapper;

import com.epam.digital.data.platform.bpms.el.juel.AbstractApplicationContextAwareJuelFunction;
import java.lang.reflect.Method;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.springframework.stereotype.Component;

/**
 * A FunctionMapper which resolves the custom juel functions for Expression Language.
 * <p>
 * <b>In case of duplicate function names will resolve first found function</b>
 *
 * @see AbstractApplicationContextAwareJuelFunction Base class for all custom JUEL functions
 */
@Component
@RequiredArgsConstructor
public class CompositeApplicationContextAwareJuelFunctionMapper extends FunctionMapper {

  private final List<AbstractApplicationContextAwareJuelFunction> juelFunctions;

  @Override
  public Method resolveFunction(String prefix, String localName) {
    return juelFunctions.stream()
        .map(AbstractApplicationContextAwareJuelFunction::getJuelFunctionMethod)
        .filter(method -> method.getName().equals(localName))
        .findFirst().orElse(null);
  }
}
