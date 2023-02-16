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

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessDefinitionAuthDto;
import java.util.List;
import java.util.function.ToIntFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

/**
 * Repository service that is used to execute a custom sql query to create/delete authorizations in
 * Camunda {@link SqlSessionFactory}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MybatisAuthorizationRepositoryService {

  private static final String INSERT_PI_AUTH_QUERY_ID = "insertProcessInstanceAuthorizations";
  private static final String INSERT_PD_AUTH_QUERY_ID = "insertProcessDefinitionAuthorizations";
  private static final String DELETE_AUTH_QUERY_ID = "deleteAuthorizations";

  private final SqlSessionFactory sqlSessionFactory;

  /**
   * Create authorizations for process instances with permissions = 'CREATE', resource_id = '*'.
   *
   * @param groups list of group names for which authorizations will be created.
   * @return count of created authorizations.
   */
  public int createProcessInstanceAuthorizations(List<String> groups) {
    log.debug("Execute insert query with id: {}", INSERT_PI_AUTH_QUERY_ID);
    var result = execute(
        session -> session.insert(getFullQueryPath(INSERT_PI_AUTH_QUERY_ID), groups));
    log.debug("Authorizations created: {}, query id {}", result, INSERT_PI_AUTH_QUERY_ID);
    return result;
  }

  /**
   * Create authorizations for process definitions with permissions = 'READ', 'CREATE_INSTANCE'.
   *
   * @param definitions list of {@link DdmProcessDefinitionAuthDto} for which authorizations will be
   *                    created.
   * @return count of created authorizations.
   */
  public int createProcessDefinitionAuthorizations(List<DdmProcessDefinitionAuthDto> definitions) {
    log.debug("Execute insert query with id: {}", INSERT_PD_AUTH_QUERY_ID);
    var result = execute(
        session -> session.insert(getFullQueryPath(INSERT_PD_AUTH_QUERY_ID), definitions));
    log.debug("Authorizations created: {}, query id {}", result, INSERT_PD_AUTH_QUERY_ID);
    return result;
  }

  /**
   * Remove authorizations for process-instances (permissions = 'CREATE', resource_id = '*') and
   * process-definitions (permissions = 'READ', 'CREATE_INSTANCE').
   *
   * @return count of removed authorizations.
   */
  public int deleteAuthorizations() {
    log.debug("Execute delete query with id: {}", DELETE_AUTH_QUERY_ID);
    var result = execute(session -> session.delete(getFullQueryPath(DELETE_AUTH_QUERY_ID)));
    log.debug("Authorizations deleted: {}, query id {}", result, DELETE_AUTH_QUERY_ID);
    return result;
  }

  private int execute(ToIntFunction<SqlSession> function) {
    try (var session = sqlSessionFactory.openSession()) {
      return function.applyAsInt(session);
    }
  }

  private String getFullQueryPath(String queryId) {
    return String.format("handler.authorization.%s", queryId);
  }
}