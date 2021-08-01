package com.epam.digital.data.platform.bpms.it.config;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestS3ObjectCephServiceImpl implements S3ObjectCephService {

  private final Map<String, S3Object> storage = new HashMap<>();

  public Map<String, S3Object> getStorage() {
    return storage;
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
    return Objects.requireNonNull(storage.get(key)).getObjectMetadata();
  }

  @Override
  public Optional<S3Object> get(String key) {
    S3Object s3Object = storage.get(key);
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
    return Optional.of(keys.stream().map(k -> storage.get(k).getObjectMetadata())
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
}
