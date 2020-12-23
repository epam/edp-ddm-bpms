package ua.gov.mdtu.ddm.lowcode.integration.ceph.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.exception.CephCommuncationException;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.exception.MisconfigurationException;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.impl.CephServiceS3Impl;

@RunWith(MockitoJUnitRunner.class)
public class CephServiceS3ImplTest {

  public static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private CephServiceS3Impl cephServiceS3Impl;
  @Mock
  private AmazonS3 conn;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(cephServiceS3Impl, "cephAmazonS3", conn);
  }

  @Test
  public void getContentWithEmptyBuckets() {
    var key = "key";

    var ex = assertThrows(MisconfigurationException.class,
        () -> cephServiceS3Impl.getContent(CEPH_BUCKET_NAME, key));

    assertThat(ex.getMessage(), is(String.format("Bucket %s hasn't found", CEPH_BUCKET_NAME)));
  }

  @Test
  public void getContentWithErrors() {
    var key = "key";

    when(conn.listBuckets()).thenThrow(new RuntimeException("message"));
    var ex = assertThrows(CephCommuncationException.class,
        () -> cephServiceS3Impl.getContent(CEPH_BUCKET_NAME, key));

    assertThat(ex.getMessage(), is("message"));
  }

  @Test
  public void getContentWithExistingBucket() {
    var key = "key";
    var value = "value";

    when(conn.listBuckets()).thenReturn(Collections.singletonList(new Bucket(CEPH_BUCKET_NAME)));
    when(conn.getObjectAsString(CEPH_BUCKET_NAME, key)).thenReturn(value);

    var result = cephServiceS3Impl.getContent(CEPH_BUCKET_NAME, key);

    assertSame(result, value);
  }

  @Test
  public void putContentWithExistingBucket() {
    var key = "key";
    var value = "value";

    when(conn.listBuckets()).thenReturn(Collections.singletonList(new Bucket(CEPH_BUCKET_NAME)));

    cephServiceS3Impl.putContent(CEPH_BUCKET_NAME, key, value);

    verify(conn, never()).createBucket(CEPH_BUCKET_NAME);
    verify(conn).putObject(CEPH_BUCKET_NAME, key, value);
  }

}
