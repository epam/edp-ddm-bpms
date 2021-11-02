package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.cmd.GetStartFormKeysCmd;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchFormServiceImpl extends ServiceImpl implements BatchFormService {

  @Override
  public Map<String, String> getStartFormKeys(List<String> processDefinitionIds) {
    log.info("Getting start form map for process definitions - {}", processDefinitionIds);
    var result = commandExecutor.execute(new GetStartFormKeysCmd(processDefinitionIds));
    log.info("Found {} start forms", result.size());
    return result;
  }
}
