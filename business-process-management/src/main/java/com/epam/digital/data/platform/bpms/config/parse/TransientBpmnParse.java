package com.epam.digital.data.platform.bpms.config.parse;

import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * The class represents an implementation of {@link BpmnParse} that is used for overriding default
 * camunda bpmn parsing for saving properties transient flag.
 */
public class TransientBpmnParse extends BpmnParse {

  public TransientBpmnParse(BpmnParser parser) {
    super(parser);
  }

  @Override
  protected void parseActivityInputOutput(Element activityElement, ActivityImpl activity) {
    super.parseActivityInputOutput(activityElement, activity);
    var ioMapping = activity.getIoMapping();
    if (ioMapping == null) {
      return;
    }
    List<InputParameter> inputParameters = ioMapping.getInputParameters().stream()
        .map(inputParameter -> new TransientInputParameter(inputParameter.getName(),
            inputParameter.getValueProvider())).collect(Collectors.toList());
    ioMapping.setInputParameters(inputParameters);
  }
}
