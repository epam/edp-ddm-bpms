### Local development  
1. create `low-code/mock-server` image:
    * download [low-code/mock-server](https://gitbud.epam.com/mdtu-ddm/low-code-platform/mock/data-factory-mock-server) project
    * open `data-factory-mock-server` project folder then create a docker image: `mvn package -P docker`
2. run docker compose: `docker-compose -f docker-compose-local.yml up`
3. run spring boot application using dev profile
    * `mvn spring-boot:run -Drun.profiles=dev` OR using appropriate functions of your IDE.
