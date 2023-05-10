## **Microservices, Asynchronous Communication, Containerization & Distributed Tracing**
[![Build & Test](https://github.com/emirhanc/operatorservices/actions/workflows/buildandtest.yml/badge.svg?branch=main)](https://github.com/emirhanc/operatorservices/actions/workflows/buildandtest.yml)
## operatorservices
Sample project to emulate operations of an arbitrary wireless service provider, such as registering/removing customers, adding/removing customer accounts and making/witdrawing package purchases utilizing microservice architecture and synchronous/asynchronous communication with Spring Boot and Apache Kafka respectively. 

![image](https://user-images.githubusercontent.com/29688260/210116407-4810c58e-f8ac-4a08-bbd0-f5fcd97e8d08.png)
## Zipkin Dependency Graph

![zipkin](https://user-images.githubusercontent.com/29688260/210241621-36b7de1b-d93d-42b7-90c6-4e1a9df73f5b.gif)

## Installation
Docker Engine and Docker CLI are needed as they are prerequisites to be able to use Docker. There are 2 convenient ways to start up the project:
1. Starting the services from project modules and starting the already dockerized rest on docker. 
2. Building images from modules and using docker compose to start all the services on docker. When it comes to building docker images from Spring Boot applications there's more than 2 ways to achieve it. One of them is using `mvn spring-boot:build-image` command and another one is using `docker build` command with proper Dockerfiles. 

If `mvn spring-boot:build-image` command is used at a module's root, it will build an image with the module's name and with the tag "0.0.1-SNAPSHOT". After building the images with `mvn spring-boot:build-image` command, project can be started with `docker compose up` at the project root. Also, Dockerfiles can be found in the respective modules. 

An example usage flow can be found in the demo tour section below.

## NOTES
***I am aware that Consul is not necessary when all the services are in the same docker network. There are 2 reasons I am keeping it: 1. Testing the modules outside the docker network 2. Demo purposes.***

***Known Issue:*** After migrating from 2.7.5 to 3.0.6, I had to remove Sleuth and replace it with Micrometer. It does not officially support sending traces over Kafka yet. Although I managed to make it work like Sleuth, reply traces when using replyingKafkaTemplate(see purchase-order-service) are not being sent to Kafka. 

***This project has a lot to discover and talk about. I am planning to write several blog posts on it and expand the explanation here as I further progress through the tasks in the todo section.***

## TODO
* ~~*Add simple project explanation & installation guide*~~
* ~~*Add Dockerfiles*~~
* Replace Zipkin with the ELK Stack
* Add HCP Vault integration
* ~~*Upgrade to JDK 17+*~~
* ~~*Migrate app to Spring Boot 3.0*~~ (See core-service, purchase-order-service & notification-service)
* ~~*Decouple core-service*~~ (I am not really sure it is necessary as it is already tightly coupled)
* Bring K8s on the scene

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
* Containerization & Multi-stage Docker build
* Relational Database & Many-to-Many Relationship
* NoSQL Databases & Distributed Cache 
* Continuous Integration

### Tech Stack
* ~~*Java 11*~~ Java 17
* Kotlin 1.7.20
* JUnit 5
* Spring Boot (2.7.5 & 3.0.6)
* Spring Data JPA
* Spring HATEOAS
* Spring Cloud Config
* Spring Cloud Consul
* Spring Cloud Gateway
* Spring Cloud Sleuth
* Spring Cloud Zipkin
* Micrometer Tracing
* OpenZipkin Brave
* Apache Kafka
* Apache Kafka Raft (KRaft)
* HashiCorp Consul
* Docker
* Docker Compose
* PostgreSQL
* Redis
* Hazelcast
* H2 Database

### Tools
* GitHub Actions
* Spring Boot Actuator
* Zipkin UI
* Provectus UI for Apache Kafka
* MapStruct
* Apache Maven
* Dockerfile
* Buildpacks
* Open API/Swagger
* Postman
* Adminer

## Demo Tour
Swagger can be used as the Core Service's API UI to understand the service's capabilities. A perfect flow would be as the following:

**1. Create a customer**
```json
    "name": "Hedy",
    "surname": "Lamarr",
    "email": "hedylamarr@fhss.com",
    "password": "astrongpass"
```
```json
    "id": "23b189b7-cf7c-4b05-8b0c-5dab66cc15aa",
    "creationDate": "2023-01-02T15:30:03.157718",
    "name": "Hedy",
    "surname": "Lamarr",
    "email": "hedylamarr@fhss.com",
    "password": "astrongpass",
    "accounts": [],
    "_links": {
        "self": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa"
        },
        "collection": {
            "href": "http://localhost:8083/v1/customers"
        },
        "accounts": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa/accounts"
        }
    }
```
**2. Create an account to the customer using the customer id**
```json
    "customerId": "23b189b7-cf7c-4b05-8b0c-5dab66cc15aa",
    "accountBalance": "2000",
    "tariffType": "PREMIUM"
```
```json
    "id": "e5a3bd4f-ada4-46ec-b195-98951e76e3d7",
    "creationDate": "2023-01-02T15:39:51.376369",
    "customer": {
        "id": "23b189b7-cf7c-4b05-8b0c-5dab66cc15aa",
        "name": "Hedy",
        "surname": "Lamarr",
        "email": "hedylamarr@fhss.com"
    },
    "tariffType": "PREMIUM",
    "accountBalance": 2000,
    "purchases": [],
    "_links": {
        "self": {
            "href": "http://localhost:8083/v1/accounts/e5a3bd4f-ada4-46ec-b195-98951e76e3d7"
        },
        "collection": {
            "href": "http://localhost:8083/v1/accounts"
        },
        "purchases": {
            "href": "http://localhost:8083/v1/accounts/e5a3bd4f-ada4-46ec-b195-98951e76e3d7/purchases"
        }
    }
```
**3. Create a package as you like and make sure the package is purchasable**
```json
    "name": "New Package",
    "packageType": "COMBO",
    "purchasable": true,
    "duration": 6
```
```json
    "id": 130,
    "name": "New Package",
    "packageType": "COMBO",
    "duration": 6,
    "purchasable": true,
    "_links": {
        "self": {
            "href": "http://localhost:8083/v1/packages/130"
        },
        "collection": {
            "href": "http://localhost:8083/v1/packages"
        },
        "accounts": {
            "href": "http://localhost:8083/v1/packages/130/accounts"
        }
    }
```
**4. Create a purchase at the** `localhost:8083/v1/purchase-order` **endpoint using account id, like below:**

```json
  "accountId": "e5a3bd4f-ada4-46ec-b195-98951e76e3d7",
  "subPackageId": 130,
  "packagePrice": 86
```
**If everything was done correctly, response should look like this:**

```json
    "id": "e339ada8-6528-4571-916e-e6fad2e003f3",
    "purchaseDate": "2023-01-02T15:49:19.648971",
    "subPackage": {
        "id": 130,
        "name": "New Package",
        "packageType": "COMBO",
        "duration": 6,
        "purchasable": true
    }
```
**5. At** `localhost:8083/v1/customers/{customerId}`, **check if everything worked as expected and purchase amount was withdrawn**

```json
    "id": "23b189b7-cf7c-4b05-8b0c-5dab66cc15aa",
    "creationDate": "2023-01-02T15:30:03.157718",
    "name": "Hedy",
    "surname": "Lamarr",
    "email": "hedylamarr@fhss.com",
    "accounts": [
        {
            "id": "e5a3bd4f-ada4-46ec-b195-98951e76e3d7",
            "creationDate": "2023-01-02T15:39:51.376369",
            "tariffType": "PREMIUM",
            "accountBalance": 1914.00,
            "purchases": [
                {
                    "id": "e339ada8-6528-4571-916e-e6fad2e003f3",
                    "purchaseDate": "2023-01-02T15:49:19.648971",
                    "subPackage": {
                        "id": 130,
                        "name": "New Package",
                        "packageType": "COMBO",
                        "duration": 6,
                        "purchasable": true
                    }
                }
            ]
        }
    ],
    "_links": {
        "self": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa"
        },
        "collection": {
            "href": "http://localhost:8083/v1/customers"
        },
        "accounts": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa/accounts"
        }
    }
```
**6. Delete the purchase at** `localhost:8083/v1/purchases/{purchaseId}` **and confirm that status is "204 No Content"**

**7. Lastly, at** `localhost:8083/v1/customers/{customerId}`**, check everything was updated**
```json
   "id": "23b189b7-cf7c-4b05-8b0c-5dab66cc15aa",
    "creationDate": "2023-01-02T15:30:03.157718",
    "name": "Hedy",
    "surname": "Lamarr",
    "email": "hedylamarr@fhss.com",
    "accounts": [
        {
            "id": "e5a3bd4f-ada4-46ec-b195-98951e76e3d7",
            "creationDate": "2023-01-02T15:39:51.376369",
            "tariffType": "PREMIUM",
            "accountBalance": 2000.00,
            "purchases": []
        }
    ],
    "_links": {
        "self": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa"
        },
        "collection": {
            "href": "http://localhost:8083/v1/customers"
        },
        "accounts": {
            "href": "http://localhost:8083/v1/customers/23b189b7-cf7c-4b05-8b0c-5dab66cc15aa/accounts"
        }
    }
```
