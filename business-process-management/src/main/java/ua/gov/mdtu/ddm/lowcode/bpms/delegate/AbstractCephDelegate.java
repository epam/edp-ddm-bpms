package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import ua.gov.mdtu.ddm.general.integration.ceph.dto.FormDataDto;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to work with ceph.
 * Contains methods for serialize/deserialize form data using {@link ObjectMapper}.
 */
@AllArgsConstructor
public abstract class AbstractCephDelegate implements JavaDelegate {

  private final ObjectMapper objectMapper;

  /**
   * Convert string data to {@link FormDataDto} entity
   *
   * @param formData data for converting
   * @return {@link FormDataDto} entity
   * @throws IllegalStateException if the data cannot be converted
   */
  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(formData, FormDataDto.class);
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalStateException("Couldn't deserialize form data", ex);
    }
  }

  /**
   * Convert {@link FormDataDto} entity to string data
   *
   * @param formData data for converting
   * @return string data
   * @throws IllegalStateException if the data cannot be converted
   */
  protected String serializeFormData(FormDataDto formData) {
    try {
      return this.objectMapper.writeValueAsString(formData);
    } catch (JsonProcessingException ex) {
      ex.clearLocation();
      throw new IllegalStateException("Couldn't serialize form data", ex);
    }
  }
}
