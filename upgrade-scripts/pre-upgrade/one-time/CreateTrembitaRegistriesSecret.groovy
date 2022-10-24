void call() {
    sh "oc apply -f ./resources/trembita-registries-secrets.yaml -n $NAMESPACE"
}

return this;