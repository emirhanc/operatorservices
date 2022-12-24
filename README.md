## **Microservices, Asynchronous Communication, Containerization & Distributed Tracing**

## operatorservices
Sample project to emulate operations of an arbitrary wireless service provider, such as registering/removing customers, adding/removing customer accounts and making/witdrawing package purchases utilizing microservice architecture and synchronous/asynchronous communication with Spring Boot and Apache Kafka respectively. 

![project image](https://user-images.githubusercontent.com/29688260/209450213-1a807863-9be2-4aff-a668-9682c11690c3.png)

## Installation
Docker Engine and Docker CLI are needed as they are prerequisites to be able to use Docker. There are 2 convenient ways to start up the project:
1. Starting the services from project modules and starting the already dockerized rest on docker. 
2. Building images from modules and using docker compose to start all the services on docker. When it comes to building docker images from Spring Boot applications there's more than 2 ways to achieve it. One of them is using `mvn spring-boot:build-image` command and another one is using `docker build` command with proper Dockerfiles. 

If `mvn spring-boot:build-image` command is used at a module's root, it will build an image with the module's name and with the tag "0.0.1-SNAPSHOT". After building the images with `mvn spring-boot:build-image` command, project can be started with `docker compose up` at the project root. Dockerfiles will also be added to the project. 

## Demo Tour
Swagger can be used as the Core Service's API UI to understand the service's capabilities. A perfect flow would be as the following:
1. Create a customer
2. Create an account to the customer using the customer id
3. Create a package as you like
4. Create a purchase at the localhost:8083/v1/purchase-order endpoint using account id, like below  :

```json
  "accountId": "7e41a7af-eba8-4b79-b29b-02d58330d7c2",
  "subPackageId": 15,
  "packagePrice": 0
```

If everything done correctly, response should look like this:

```json
{
    "id": "3fc430db-b1a0-4716-85b7-d2d9c21eebb2",
    "purchaseDate": "2022-12-24T21:05:30.798079",
    "subPackage": {
        "id": 15,
        "name": "TooCheapToMeter",
        "packageType": "COMBO",
        "duration": 12,
        "purchasable": true
    }
}
```
This project has a lot to discover and talk about. I am planning to write several blog posts on it and expand the explanation here as I further progress through the tasks in the todo section.

## TODO
* ~~*Add simple project explanation & installation guide*~~
* Add Dockerfiles
* Replace Zipkin with the ELK Stack
* Add HCP Vault integration
* Migrate app to Spring Boot 3.0
* Upgrade to JDK 17+
* Bring k8s on the scene

### Practices
* RESTful API
* Hypermedia as the Engine of Application State (HATEOAS)
* API Gateway & Server Side Load Balancing
* Unit & Integration Tests
* Input Validation
* Custom Exceptions
* Global Exception Handling
* Asynchronous/Synchronous Communication between microservices 
* Distributed Tracing
* Centralized Config
* Controller, Service Layer & DTO Patterns
* Containerization & Dockerize
* Relational Database & Many-to-Many Relationship

### Tech Stack
* Java 11
* Kotlin 1.7.20
* Spring Boot
* Spring Data JPA
* Spring HATEOAS
* Spring Cloud Config
* Spring Cloud Consul
* Spring Cloud Gateway
* Spring Cloud Sleuth
* Spring Cloud Zipkin
* Apache Kafka
* Apache Kafka Raft (KRaft)
* HashiCorp Consul
* Docker
* Docker Compose
* H2 Database
* JUnit 5

### Tools
* Spring Boot Actuator
* Zipkin UI
* Provectus UI for Apache Kafka
* MapStruct
* Apache Maven
* Dockerfile
* Open API/Swagger
* Postman
* Buildpacks

