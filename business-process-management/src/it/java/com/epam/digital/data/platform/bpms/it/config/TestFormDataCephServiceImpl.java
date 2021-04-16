package com.epam.digital.data.platform.bpms.it.config;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TestFormDataCephServiceImpl implements FormDataCephService {

  private String cephBucketName;

  private final Map<String, FormDataDto> storage = new HashMap<>();

  @Override
  public FormDataDto getFormData(String key) {
    verifyBucketName(cephBucketName);
    return storage.get(key);
  }

  @Override
  public void putFormData(String key, FormDataDto formDataDto) {
    verifyBucketName(cephBucketName);
    storage.put(key, formDataDto);
  }

  public void clearStorage() {
    storage.clear();
  }

  private void verifyBucketName(String bucketName) {
    if (!cephBucketName.equals(bucketName)) {
      throw new MisconfigurationException("Bucket " + bucketName + " hasn't found");
    }
  }
}
