package mdtu.ddm.bpms.rest.controller;

import lombok.extern.slf4j.Slf4j;
import mdtu.ddm.bpms.rest.dto.RequestsDto;
import mdtu.ddm.bpms.rest.dto.StatusDto;
import mdtu.ddm.bpms.service.RequestStatusService;
import mdtu.ddm.bpms.service.model.RequestStatusModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom controller that allows to retrieve process instance status in a convenient way
 */
@Slf4j
@RestController
public class RequestStatusController {

    private final RequestStatusService requestStatusService;

    @Autowired
    public RequestStatusController(RequestStatusService requestStatusService) {
        this.requestStatusService = requestStatusService;
    }

    /**
     * Retrieve current execution step in a custom form
     *
     * @param processInstanceId to search for
     * @return StatusDto with the string representation of the current execution step
     */
    @GetMapping(value = "/custom/process-instance/{processInstanceId}/status")
    public StatusDto getRequestStatus(@PathVariable("processInstanceId") String processInstanceId) {
        log.info("get process instance status: {}", processInstanceId);

        RequestStatusModel ret = requestStatusService.getRequestStatus(processInstanceId);
        log.info("process instance status is: {}", ret);
        return new StatusDto(processInstanceId, null, ret.getMessage());
    }

    /**
     * Retrieve current execution step in a custom form for specified process instance ids
     *
     * @param dto that contains process instance ids
     * @return a list of StatusDto with the string representation of the current execution step
     */
    @PutMapping(value = "/custom/process-instance/status")
    public List<StatusDto> getRequestStatuses(@RequestBody RequestsDto dto) {
        log.info("get process instance statusus: {}", dto);

        if (dto == null || dto.getProcessInstanceIds() == null || dto.getProcessInstanceIds().size() == 0) {
            return Collections.emptyList();
        }

        List<RequestStatusModel> list = requestStatusService.getRequestStatuses(dto.getProcessInstanceIds());
        log.info("process instance status is: {}", list);

        return list.stream()
                .map(m -> new StatusDto(
                        m.getProcessInstanceId(),
                        m.getStatus() == null ? null : m.getStatus().toString(),
                        m.getMessage()))
                .collect(Collectors.toList());

    }
}
