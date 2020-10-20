package mdtu.ddm.bpms.service;

import lombok.extern.slf4j.Slf4j;
import mdtu.ddm.bpms.service.model.RequestStatus;
import mdtu.ddm.bpms.service.model.RequestStatusModel;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that uses camunda {@link RuntimeService} to obtain process instance status
 */
@Slf4j
@Service
public class RequestStatusService {

    private final RuntimeService runtimeService;

    public RequestStatusService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Retrieve process instance status in string format by process instance id
     *
     * @param processInstanceId to identify process instance
     * @return representation of process instance status
     */
    public RequestStatusModel getRequestStatus(String processInstanceId) {

        ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
        if (activityInstance == null) {
            log.info("no process instance found by id: {}", processInstanceId);
            return new RequestStatusModel(processInstanceId, RequestStatus.ERROR, "No process instance found");
        }

        if (activityInstance.getChildActivityInstances().length == 0) {
            log.info("no getChildActivityInstances found by id: {}", processInstanceId);
            return new RequestStatusModel(processInstanceId, null, "Process instance has been completed");
        }

        String activityName = activityInstance.getChildActivityInstances()[0].getActivityName();
        log.info("current instance name: {}, processInstance: {}", activityName, processInstanceId);

        return new RequestStatusModel(processInstanceId, null, activityName);
    }

    /**
     * Retrieve process instance statuses in string format by process instance ids
     *
     * @param processInstanceIds a list of process instance ids to identify process instance
     * @return representation of process instance statuses
     */
    public List<RequestStatusModel> getRequestStatuses(List<String> processInstanceIds) {

        List<RequestStatusModel> ret = new ArrayList<>(processInstanceIds.size());

        processInstanceIds.forEach(o -> ret.add(getRequestStatus(o)));

        return ret;
    }

}
