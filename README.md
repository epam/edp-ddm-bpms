# business-process-management

##### The main purpose of the business process management service is to extend the Camunda API:

* `The application brings the following functionality:`
    * execution of business processes described in BPMN notation;
    * execution of business rules described in DMN notation;
    * execution of scripts in the scope of business process tasks;
    * implementation of extensions of tasks and connectors.


##### Spring Actuator configured with Micrometer extension for exporting data in prometheus-compatible format.
*End-point:* <service>:<port>/actuator/prometheus

*Prometheus configuration example (prometheus.yml):*

```
global:
  scrape_interval: 10s
scrape_configs:
  - job_name: 'spring_micrometer'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['< service >:< port >']
```

##### Spring Sleuth configured for Istio http headers propagation:

- x-access-token
- x-request-id
- x-b3-traceid
- x-b3-spanid
- x-b3-parentspanid
- x-b3-sampled
- x-b3-flags
- b3

##### Running the tests:

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### Local development:

1. `application-local.yml` is configuration file for local development;
2. create `low-code/mock-server` image:
    * download [low-code/mock-server](https://gitbud.epam.com/mdtu-ddm/low-code-platform/mock/data-factory-mock-server) project
    * open `data-factory-mock-server` project folder then create a docker image: `mvn package -P docker`
3. run docker compose: `docker-compose -f docker-compose-local.yml up`
4. to interact with `digital signature ops` service, set `dso.url` variable as an environment
   variable or specify it in the configuration file;
5. the application will be available on: http://localhost:8080  
6. logging settings (*level,pattern,output file*) specified in the configuration file;
7. database settings (*driver-class-name,url,username,password*) specified in the configuration file:
    * by default `postgresql`;
8. run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
9. to check how it works, use the `user process management` or `user task management` service.   
  

##### Logging:

* `Default:`
    * For classes with annotation RestController/Service, logging is enabled by default for all public methods of a class;
* `To set up logging:`
    * *@Logging* - can annotate a class or method to enable logging;
    * *@Confidential* - can annotate method or method parameters to exclude confidential data from logs:
        - For a method - exclude the result of execution;
        - For method parameters - exclude method parameters;
