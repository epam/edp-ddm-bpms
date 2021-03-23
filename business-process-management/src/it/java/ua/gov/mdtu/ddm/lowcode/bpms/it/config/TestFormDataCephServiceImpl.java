package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ua.gov.mdtu.ddm.general.integration.ceph.dto.FormDataDto;
import ua.gov.mdtu.ddm.general.integration.ceph.exception.MisconfigurationException;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;

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
