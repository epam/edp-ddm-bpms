package com.epam.digital.data.platform.bpms.controller;

import javax.ws.rs.Path;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.impl.DefaultProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.JaxRsTwoDefaultProcessEngineRestServiceImpl;

/**
 * Lowcode process engine resource that provides instantiations of all REST resources.
 * It allows changing default rest resources and add extra logic.
 */
@Path(DefaultProcessEngineRestServiceImpl.PATH)
public class LowCodeDefaultProcessEngineRestController extends JaxRsTwoDefaultProcessEngineRestServiceImpl {

  @Override
  @Path(TaskRestService.PATH)
  public TaskRestService getTaskRestService() {
    return this.getTaskRestService(null);
  }

  @Override
  public TaskRestService getTaskRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    TaskController subResource = new TaskController(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

}
