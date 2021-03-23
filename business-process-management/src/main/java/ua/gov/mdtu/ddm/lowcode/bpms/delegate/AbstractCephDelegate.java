package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import ua.gov.mdtu.ddm.general.integration.ceph.dto.FormDataDto;

@AllArgsConstructor
public abstract class AbstractCephDelegate implements JavaDelegate {

  private final ObjectMapper objectMapper;

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(formData, FormDataDto.class);
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalStateException("Couldn't deserialize form data", ex);
    }
  }

  protected String serializeFormData(FormDataDto formData) {
    try {
      return this.objectMapper.writeValueAsString(formData);
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalStateException("Couldn't serialize form data", ex);
    }
  }
}
