package ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.exception.CephCommuncationException;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.exception.MisconfigurationException;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.CephService;

@Service
@RequiredArgsConstructor
public class CephServiceS3Impl implements CephService {

  private final AmazonS3 cephAmazonS3;

  public void putContent(String cephBucketName, String key, String content) {
    assertBucketExists(cephBucketName);
    execute(() -> cephAmazonS3.putObject(cephBucketName, key, content));
  }

  public String getContent(String cephBucketName, String key) {
    assertBucketExists(cephBucketName);
    return execute(() -> cephAmazonS3.getObjectAsString(cephBucketName, key));
  }

  private <T> T execute(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException exception) {
      throw new CephCommuncationException(exception.getMessage(), exception);
    }
  }

  private void assertBucketExists(String cephBucketName) {
    var buckets = execute(cephAmazonS3::listBuckets);
    buckets.stream()
        .filter(bucket -> bucket.getName().equals(cephBucketName))
        .findFirst()
        .orElseThrow(() -> new MisconfigurationException(
            String.format("Bucket %s hasn't found", cephBucketName)));
  }
}
