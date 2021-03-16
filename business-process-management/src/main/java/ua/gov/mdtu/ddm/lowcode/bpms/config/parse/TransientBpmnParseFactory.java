package ua.gov.mdtu.ddm.lowcode.bpms.config.parse;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of BpmnParseFactory that creates an instance of TransientBpmnParse
 */
@Component
public class TransientBpmnParseFactory implements BpmnParseFactory {

  @Override
  public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
    return new TransientBpmnParse(bpmnParser);
  }
}
