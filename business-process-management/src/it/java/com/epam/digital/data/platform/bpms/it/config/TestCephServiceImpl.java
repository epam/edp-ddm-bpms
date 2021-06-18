package com.epam.digital.data.platform.bpms.it.config;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.camunda.spin.Spin;

@Setter
@Getter
@AllArgsConstructor
public class TestCephServiceImpl implements CephService, FormDataCephService {

  private final Map<String, String> storage = new HashMap<>();

  private String cephBucketName;
  private ObjectMapper objectMapper;

  @Override
  public String getContent(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    return storage.get(key);
  }

  @Override
  public void putContent(String cephBucketName, String key, String content) {
    verifyBucketName(cephBucketName);
    storage.put(key, content);
  }

  @Override
  public void deleteContent(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    storage.remove(key);
  }

  @SneakyThrows
  @Override
  public FormDataDto getFormData(String key) {
    var content = getContent(cephBucketName, key);
    if (content == null) {
      return null;
    }
    return objectMapper.readValue(storage.get(key), FormDataDto.class);
  }

  @Override
  public void putFormData(String key, FormDataDto formDataDto) {
    putContent(cephBucketName, key, Spin.JSON(formDataDto).toString());
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