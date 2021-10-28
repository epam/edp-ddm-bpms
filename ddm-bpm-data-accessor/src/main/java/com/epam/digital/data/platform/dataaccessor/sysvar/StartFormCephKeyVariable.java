package com.epam.digital.data.platform.dataaccessor.sysvar;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessor;

public class StartFormCephKeyVariable extends BaseNamedVariableAccessor<String> {

  public static final String START_FORM_CEPH_KEY_VARIABLE_NAME = "start_form_ceph_key";

  public StartFormCephKeyVariable(VariableAccessorFactory variableAccessorFactory) {
    super(START_FORM_CEPH_KEY_VARIABLE_NAME, false, variableAccessorFactory);
  }
}
