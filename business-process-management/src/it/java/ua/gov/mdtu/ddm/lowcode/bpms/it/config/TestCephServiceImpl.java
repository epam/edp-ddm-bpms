package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ua.gov.mdtu.ddm.general.integration.ceph.exception.MisconfigurationException;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;

@Setter
@Getter
@AllArgsConstructor
public class TestCephServiceImpl implements CephService {

  private final Map<String, String> storage = new HashMap<>();

  private String cephBucketName;

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

  public void clearStorage() {
    storage.clear();
  }

  private void verifyBucketName(String bucketName) {
    if (!cephBucketName.equals(bucketName)) {
      throw new MisconfigurationException("Bucket " + bucketName + " hasn't found");
    }
  }
}