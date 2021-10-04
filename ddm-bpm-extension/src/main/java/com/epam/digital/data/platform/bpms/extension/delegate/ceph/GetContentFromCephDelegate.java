package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get data from ceph
 * as string using {@link CephService} service.
 */
@Slf4j
@Component(GetContentFromCephDelegate.DELEGATE_NAME)
public class GetContentFromCephDelegate extends BaseCephDelegate {

  public static final String DELEGATE_NAME = "getContentFromCephDelegate";

  public GetContentFromCephDelegate(@Value("${ceph.bucket}") String cephBucketName,
      CephService cephService) {
    super(cephBucketName, cephService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var key = keyVariable.from(execution).get();

    log.debug("Start getting content by key {}", key);
    var content = cephService.getContent(cephBucketName, key).orElse(null);
    log.debug("Got content by key {}", key);

    contentVariable.on(execution).set(content);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}