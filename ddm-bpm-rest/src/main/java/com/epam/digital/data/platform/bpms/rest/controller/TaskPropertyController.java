/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.rest.controller;

import com.epam.digital.data.platform.bpms.rest.service.TaskPropertyService;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The class represents a controller that contains endpoint for getting extended task property
 */
@Component
@RequiredArgsConstructor
@Path("/extended/task")
public class TaskPropertyController {

  private final TaskPropertyService taskPropertyService;

  /**
   * GET method for getting extended task properties. Returns a map, where key - property name,
   * value - property value.
   * <p>
   * This method returns an empty map if the task has no properties or task with this taskId is
   * absent.
   *
   * @param taskId task identifier
   * @return a map containing the properties of the task
   */
  @GET
  @Path("/{id}/extension-element/property")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getTaskProperty(@PathParam("id") String taskId) {
    return taskPropertyService.getTaskProperty(taskId);
  }
}
