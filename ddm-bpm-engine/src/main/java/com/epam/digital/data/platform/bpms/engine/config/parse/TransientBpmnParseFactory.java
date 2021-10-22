package com.epam.digital.data.platform.bpms.engine.config.parse;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BpmnParseFactory} that is used for creating an
 * instance of custom {@link  TransientBpmnParse} parser.
 */
@Component
public class TransientBpmnParseFactory implements BpmnParseFactory {

  @Override
  public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
    return new TransientBpmnParse(bpmnParser);
  }
}
