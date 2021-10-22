package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.CephKeyProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CephKeyProviderTest {

  private CephKeyProvider cephKeyProvider;

  @Before
  public void init() {
    cephKeyProvider = new CephKeyProvider();
  }

  @Test
  public void testGeneratingCephKey() {
    var expectedKey = "process/testProcessInstanceId/task/testTaskDefinitionKey";
    var taskDefinitionKey = "testTaskDefinitionKey";
    var processInstanceId = "testProcessInstanceId";

    var actualKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    assertThat(actualKey).isEqualTo(expectedKey);
  }
}
