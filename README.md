# Microservice Visualization by Dependencies
All Spring Boot (Java) applications has dependencies to other applications, that mean has some library of DTO (Data transfer Object) that common use in all applications that communicate with specific application. This way we can create microservice topology.
#### Example
> * Application: customer 
> * DTO library: customer-api

## Support
> * Gradle 
> * Maven (TODO)

## Build on
* Java 17, Framework Spring Boot 3.0.x
* Angular 15.2.x, Framework Angular Material
* Gradle 7.6.1

#### Microservice architecture demo
> * Example of microservice project [Demo Project](demo)  
> * Execute Gradle task `createDependecyFile`, see [Output folder](result)  
> * Execute `MicroserviceVisualizationApplication.java` for run Backend  
> * Execute `dev` script of [microservice-visualization-webapp](microservice-visualization-webapp)  

##### Demo
![](demo/docs/demo.gif)