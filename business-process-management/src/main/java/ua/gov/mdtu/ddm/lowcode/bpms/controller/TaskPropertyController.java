package ua.gov.mdtu.ddm.lowcode.bpms.controller;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.service.TaskPropertyService;

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
