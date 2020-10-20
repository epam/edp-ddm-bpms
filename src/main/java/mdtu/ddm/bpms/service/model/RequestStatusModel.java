package mdtu.ddm.bpms.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestStatusModel {
    private String processInstanceId;
    private RequestStatus status;
    private String message;

}
