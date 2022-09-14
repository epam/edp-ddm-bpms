void call() {
    String citusPodName = sh(script: "oc get pod -l app=citus-master -n $NAMESPACE " +
            "--no-headers -o=custom-columns=NAME:.metadata.name", returnStdout: true).trim()
      sh "oc exec $citusPodName -n $NAMESPACE -- psql -dcamunda -Upostgres -c " +
              "\"UPDATE ACT_GE_PROPERTY SET VALUE_ = 2 WHERE NAME_ = 'historyLevel';\" || :"
}

return this;
