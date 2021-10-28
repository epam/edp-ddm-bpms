package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import lombok.RequiredArgsConstructor;

/**
 * Base class that is used for accessing ceph data as string using {@link CephService} service.
 */
@RequiredArgsConstructor
public abstract class BaseCephDelegate extends BaseJavaDelegate {

  protected final String cephBucketName;
  protected final CephService cephService;

  @SystemVariable(name = "key")
  protected NamedVariableAccessor<String> keyVariable;
  @SystemVariable(name = "content", isTransient = true)
  protected NamedVariableAccessor<String> contentVariable;
}
