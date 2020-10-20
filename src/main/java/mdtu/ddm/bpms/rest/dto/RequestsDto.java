package mdtu.ddm.bpms.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestsDto {
    private List<String> processInstanceIds;
}
