# Business-process-management-service

This service is used for business-process management. It's a Spring Boot application
over [Camunda Platform 7](https://github.com/camunda/camunda-bpm-platform).

## Components

Business-process-management-service provides a set of components that extend Camunda API:

* [ddm-bpm](ddm-bpm) - central module with application configuration and main class
* [ddm-bpm-api](ddm-bpm-api) - main data-transfer objects that are used in ddm-bpm-client and
  ddm-bpm-rest.
* [ddm-bpm-client](ddm-bpm-client) - Feign clients for http cross-service integration.
* [ddm-bpm-data-accessor](ddm-bpm-data-accessor) - business-process variables management.
* [ddm-bpm-engine](ddm-bpm-engine) - extending business-processes parsing and processing.
* [ddm-bpm-extension](ddm-bpm-extension) - custom business-process extensions that implemented as
  ExecutionDelegates.
* [ddm-bpm-history-event-handler](ddm-bpm-history-event-handler) - sending Camunda history events to
  external database.
* [ddm-bpm-integration-tests](ddm-bpm-integration-tests) - integration testing of whole application.
* [ddm-bpm-metrics-plugin](ddm-bpm-metrics-plugin) - plugin that adds business-process execution
  metrics in Prometheus.
* [ddm-bpm-rest](ddm-bpm-rest) - extending Camunda REST API.
* [ddm-bpm-security](ddm-bpm-security) - managing camunda authentication and authorization for user.
* [ddm-bpm-storage](ddm-bpm-storage) - managing form and file data in the storage during process
  execution.

## Service Usage

As this service is designed to be used in Kubernetes in scope of a platform there described
instruction only for usage for local development.

### Installation

#### Prerequisites

1. Installed [Java OpenJDK 11](https://openjdk.org/install/), [Maven](https://maven.apache.org/)
   and [Docker](https://www.docker.com/).
2. Maven is configured to use Nexus repository with all needed dependencies.
   > **_NOTE:_** Using Java JDK with version higher than 11 may cause some runtime problems.
   >
   > E.g.
   test [TokenCacheServiceTest.java](ddm-bpm-extension%2Fsrc%2Ftest%2Fjava%2Fcom%2Fepam%2Fdigital%2Fdata%2Fplatform%2Fbpms%2Fextension%2Fservice%2FTokenCacheServiceTest.java)
   may fail on higher versions because of _secp256k1 elliptic curve_ that is no longer supported.

#### Configuring

* Configuration can be changed
  here [application-local.yml](ddm-bpm/src/main/resources/application-local.yml).
* Any jvm attributes can be added to JAVA_OPTS environment variable in
  business-process-management-service in [docker-compose.yml](docker-compose.yml).
* In case if you don't need any service-mock or redis just delete it
  from [docker-compose.yml](docker-compose.yml).

#### Quick installation

1. Build the service
    ```shell
    mvn package
    ```
2. Copy the application jar file to root project directory
    ```shell
    cp ddm-bpm/target/ddm-bpm-1.9.0.jar target/
    ```
3. Run Docker-compose
    ```shell
    docker-compose up -d --scale redis-sentinel=3
    ```
   > **_NOTE:_**  If one of the **redis-sentinel** was failed because of already bound port, then
   likely this port was bound by other redis-sentinel service. In that case just execute the
   docker-compose again. It may require up to 3 attempts.
4. Go to http://localhost:8080/openapi to open services Swagger, or connect to localhost:5005 with
   remote debug.
5. In case if you need to rebuild the service you also need to remove service docker image:
   ```shell
   docker rmi bpms_business-process-management-service -f
   ```

#### Installation with Kafka

To install the service with Kafka, it's needed to perform next steps **before** building the jar
file:

1. Enable Kubernetes on your docker: [instruction](https://docs.docker.com/desktop/kubernetes/).
2. Deploy Strimzi by [instruction](https://strimzi.io/quickstarts/) on ```Docker Desktop```
   tab.
3. DO NOT create an Apache Kafka cluster from Strimzi instructions, instead perform
   next:
    ```shell
    kubectl apply -f docker-local/kafka/kafka-cluster.yml -n kafka 
    ```
4. Install Kafka-ui (Optional):
    1. Install [Helm](https://helm.sh/docs/intro/install/)
    2. Add helm repo:
        ```shell
       helm repo add kafka-ui https://provectus.github.io/kafka-ui-charts
       ```
    3. Install kafka-ui to ```kafka``` namespace:
       ```shell
       helm install kafka-ui kafka-ui/kafka-ui \
          --set envs.config.KAFKA_CLUSTERS_0_NAME=local \
          --set envs.config.KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka-cluster-kafka-bootstrap:9092 \
          --namespace kafka
       ```
    4. Port forward to Kafka-UI pod:
       ```shell
       kubectl -n kafka port-forward svc/kafka-ui 8083:80
       ```
    5. Go to [Kafka-UI](http://localhost:8083)
5. Define what node port is used by kafka:
   ```shell
   kubectl -n kafka get service kafka-cluster-kafka-external-bootstrap -o=jsonpath='{.spec.ports[0].nodePort}{"\n"}'
   ```
6. In [application-local.yml](ddm-bpm%2Fsrc%2Fmain%2Fresources%2Fapplication-local.yml) change:
    1. ```data-platform.kafka.enabled: true```
    2. ```data-platform.kafka.bootstrap: docker-desktop:${NODE_PORT}``` where ${NODE_PORT} is port
       defined in previous step
    3. ```data-platform.kafka.consumer.enabled: true```
7. Perform steps from [Quick installation](#quick-installation)

#### Usage

1. Generate JWT token for the service (e.g. [here](https://jwt.io/)). The token **must** have
   _preferred_username_ claim. And use it in ```Authorize```
   in [Swagger](http://localhost:8080/openapi).
   > **_NOTE:_** by default service have no authorization in local profile. This is needed if
   business-process accesses user info or uses the token in integrations.
2. Deploy executable business-process using _POST /deployment_
   > **_NOTE:_** _tenant_id_ **must** be ```null``` so disable the checkbox ```Send empty value```
   if you're using Swagger. Also, file extension **must** be ```.bpmn```. In other case you won't
   see your process in the list of processes.
3. Start business-process using _POST /process-definition/key/{key}/start_ where ```key``` is
   business-process-definition key defined in bpmn file. If your business-process requires
   start-form then it's needed to add required document to Redis first:
    1. Go to http://localhost:8081/ and login username - root, password qwerty.
    2. Select local(redis-master:6379:0) and press ```Add New Key...```
    3. Set any ```Key```, choose type ```Hash```. In field ```Field``` set ```data```. And
       in ```Value``` set needed json object.
    4. Used ```Key``` is needed to pass as variable with name ```start_form_ceph_key``` in starting
       business-process request body.
4. Mock every request that will be needed for business-process using Wiremock UI:
    1. [digital-document-service-mock](http://localhost:8082/__admin/webapp/mappings)
    2. [excerpt-service-api-mock](http://localhost:9999/__admin/webapp/mappings)
    3. [digital-signature-ops-mock](http://localhost:8100/__admin/webapp/mappings)
    4. [registry-rest-api-mock](http://localhost:8877/__admin/webapp/mappings)
    5. [keycloak-mock](http://localhost:8200/__admin/webapp/mappings)
5. Complete business-process tasks using _POST /extended/task/{id}/complete_ where ```id``` is a
   task id which can be retrieved by selecting task list (_/extended/task_ or
   _/extended/task/lightweight_).
   Every task will require to add corresponding document to Redis first:
    1. Go to http://localhost:8081/ and login username - root, password qwerty.
    2. Select local(redis-master:6379:0) and press ```Add New Key...```
    3. In ```Key``` field set
       value ```bpm-form-submissions:process/{processInstanceId}/task/{taskDefinitionKey}```
       where ```processInstanceId``` is an id of current process-instance
       and ```taskDefinitionKey``` is key of the task defined in bpmn file.
    4. Choose type ```Hash```. In field ```Field``` set ```data```. And
       in ```Value``` set needed json object.

### Testing

1. Stop the docker-compose if it's running:
    ```shell
    docker-compose stop
    ```
2. Run verify on maven:
    ```shell
    mvn verify
    ```
3. On Mac it can asc for accepting incoming network connections to _redis-server_ app.
   Click ```Accept```.

## License

The ddm-bpm-parent is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).