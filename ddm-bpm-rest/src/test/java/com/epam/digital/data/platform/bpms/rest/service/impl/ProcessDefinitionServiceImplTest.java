package com.epam.digital.data.platform.bpms.rest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.Optional;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceImplTest {

  @InjectMocks
  private ProcessDefinitionServiceImpl processDefinitionService;
  @Mock
  private ProcessEngine processEngine;
  @Mock
  private CamundaImpersonationFactory camundaImpersonationFactory;

  @Mock
  private CamundaImpersonation camundaImpersonation;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ProcessDefinition processDefinition;

  @Test
  void getProcessDefinition() {
    var processDefinitionId = "processDefinitionId";

    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(repositoryService.getProcessDefinition(processDefinitionId)).thenReturn(processDefinition);

    when(camundaImpersonationFactory.getCamundaImpersonation())
        .thenReturn(Optional.of(camundaImpersonation));

    var result = processDefinitionService.getProcessDefinition(processDefinitionId);
    assertThat(result).isSameAs(processDefinition);

    verify(camundaImpersonationFactory).getCamundaImpersonation();
    verify(camundaImpersonation).impersonate();
    verify(camundaImpersonation).revertToSelf();
  }

  @Test
  void getProcessDefinition_noAuthenticatedUser() {
    var processDefinitionId = "processDefinitionId";

    when(camundaImpersonationFactory.getCamundaImpersonation()).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalStateException.class,
        () -> processDefinitionService.getProcessDefinition(processDefinitionId));
    assertThat(exception.getMessage())
        .isEqualTo("Error occurred during accessing process definition info. "
            + "There is no user that authenticated in camunda");

    verify(camundaImpersonationFactory).getCamundaImpersonation();
  }
}
