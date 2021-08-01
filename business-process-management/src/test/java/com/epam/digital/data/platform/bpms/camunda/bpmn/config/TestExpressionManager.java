package com.epam.digital.data.platform.bpms.camunda.bpmn.config;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.epam.digital.data.platform.bpms.el.TransientVariableScopeElResolver;
import com.epam.digital.data.platform.el.juel.AbstractApplicationContextAwareJuelFunction;
import com.epam.digital.data.platform.el.juel.CompleterJuelFunction;
import com.epam.digital.data.platform.el.juel.InitiatorJuelFunction;
import com.epam.digital.data.platform.el.juel.SignSubmissionJuelFunction;
import com.epam.digital.data.platform.el.juel.SubmissionJuelFunction;
import com.epam.digital.data.platform.el.juel.ceph.CephKeyProvider;
import com.epam.digital.data.platform.el.juel.mapper.CompositeApplicationContextAwareJuelFunctionMapper;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.springframework.context.ApplicationContext;

public class TestExpressionManager extends ExpressionManager {

  public TestExpressionManager(ObjectMapper objectMapper, FormDataCephService cephService) {
    var appContext = mock(ApplicationContext.class);
    lenient().when(appContext.getBean(TokenParser.class)).thenReturn(new TokenParser(objectMapper));
    lenient().when(appContext.getBean(FormDataCephService.class)).thenReturn(cephService);
    lenient().when(appContext.getBean(CephKeyProvider.class)).thenReturn(new CephKeyProvider());

    var juelFunctionList = new ArrayList<AbstractApplicationContextAwareJuelFunction>();
    juelFunctionList.add(new InitiatorJuelFunction());
    juelFunctionList.add(new SubmissionJuelFunction());
    juelFunctionList.add(new CompleterJuelFunction());
    juelFunctionList.add(new SignSubmissionJuelFunction());

    juelFunctionList.forEach(function -> function.setApplicationContext(appContext));
    addFunctionMapper(new CompositeApplicationContextAwareJuelFunctionMapper(juelFunctionList));
  }

  @Override
  protected ELResolver createElResolver() {
    var superResolver = super.createElResolver();

    var resolver = new CompositeELResolver();
    resolver.add(new TransientVariableScopeElResolver());
    resolver.add(superResolver);

    return resolver;
  }
}
