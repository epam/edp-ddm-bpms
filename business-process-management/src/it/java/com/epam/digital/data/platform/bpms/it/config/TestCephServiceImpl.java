package com.epam.digital.data.platform.bpms.it.config;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.epam.digital.data.platform.integration.ceph.dto.CephObject;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.exception.MisconfigurationException;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.camunda.spin.Spin;

@Setter
@Getter
@AllArgsConstructor
public class TestCephServiceImpl implements CephService, FormDataCephService, S3ObjectCephService {

  private final Map<String, Object> storage = new HashMap<>();

  private String cephBucketName;
  private ObjectMapper objectMapper;

  @Override
  public Optional<String> getContent(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    return Optional.ofNullable((String) storage.get(key));
  }

  @Override
  public Optional<CephObject> getObject(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putContent(String cephBucketName, String key, String content) {
    verifyBucketName(cephBucketName);
    storage.put(key, content);
  }

  @Override
  public void putObject(String cephBucketName, String key, CephObject cephObject) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteObject(String cephBucketName, String key) {
    verifyBucketName(cephBucketName);
    storage.remove(key);
  }

  @Override
  public boolean doesObjectExist(String s, String s1) {
    return false;
  }

  @SneakyThrows
  @Override
  public Optional<FormDataDto> getFormData(String key) {
    var content = getContent(cephBucketName, key);
    if (content.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(objectMapper.readValue(content.get(), FormDataDto.class));
  }

  @Override
  public void putFormData(String key, FormDataDto formDataDto) {
    putContent(cephBucketName, key, Spin.JSON(formDataDto).toString());
  }

  @Override
  public ObjectMetadata put(String key, String contentType, Map<String, String> userMetadata,
      InputStream fileInputStream) {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(contentType);
    objectMetadata.setContentLength(1000L);
    objectMetadata.setUserMetadata(userMetadata);
    S3Object s3Object = new S3Object();
    s3Object.setObjectContent(fileInputStream);
    s3Object.setObjectMetadata(objectMetadata);
    storage.put(key, s3Object);
    return Objects.requireNonNull((S3Object) storage.get(key)).getObjectMetadata();
  }

  @Override
  public Optional<S3Object> get(String key) {
    var s3Object = (S3Object) storage.get(key);
    if (Objects.isNull(s3Object)) {
      return Optional.empty();
    }
    return Optional.of(s3Object);
  }

  @Override
  public Optional<List<ObjectMetadata>> getMetadata(List<String> keys) {
    boolean allContains = keys.stream().allMatch(storage::containsKey);
    if (!allContains) {
      return Optional.empty();
    }
    return Optional.of(keys.stream().map(k -> ((S3Object) storage.get(k)).getObjectMetadata())
        .collect(Collectors.toList()));
  }

  @Override
  public void delete(List<String> keys) {
    keys.forEach(storage::remove);
  }

  @Override
  public Boolean exist(List<String> keys) {
    return keys.stream().allMatch(storage::containsKey);
  }

  @Override
  public List<String> getKeys(String prefix) {
    return storage.keySet().stream()
        .filter(s1 -> s1.startsWith(prefix))
        .collect(Collectors.toList());
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