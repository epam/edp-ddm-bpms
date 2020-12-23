package ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service;

public interface CephService {

  String getContent(String cephBucketName, String key);

  void putContent(String cephBucketName, String key, String content);
}
