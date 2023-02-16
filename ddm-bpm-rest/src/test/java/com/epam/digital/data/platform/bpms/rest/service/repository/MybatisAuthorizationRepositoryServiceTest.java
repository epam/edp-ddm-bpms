/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionAuthDto;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisAuthorizationRepositoryServiceTest {

  @InjectMocks
  private MybatisAuthorizationRepositoryService service;
  @Mock
  private SqlSessionFactory sqlSessionFactory;
  @Mock
  private SqlSession sqlSession;

  @Test
  void shouldCreateProcessDefinitionAuthorizations() {
    var prDefinition = new DdmProcessDefinitionAuthDto("officer", "process-def-id");
    var listDefinitions = List.of(prDefinition);
    var expected = 1;

    when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
    when(sqlSession.insert("handler.authorization.insertProcessDefinitionAuthorizations",
        listDefinitions)).thenReturn(expected);

    var result = service.createProcessDefinitionAuthorizations(listDefinitions);

    assertThat(result).isSameAs(expected);
  }

  @Test
  void shouldCreateProcessInstanceAuthorizations() {
    var listDefinitions = List.of("officer");
    var expected = 1;

    when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
    when(sqlSession.insert("handler.authorization.insertProcessInstanceAuthorizations",
        listDefinitions)).thenReturn(expected);

    var result = service.createProcessInstanceAuthorizations(listDefinitions);

    assertThat(result).isSameAs(expected);
  }

  @Test
  void shouldDeleteAuthorizations() {
    var expected = 1;

    when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
    when(sqlSession.delete("handler.authorization.deleteAuthorizations")).thenReturn(expected);

    var result = service.deleteAuthorizations();

    assertThat(result).isSameAs(expected);
  }
}