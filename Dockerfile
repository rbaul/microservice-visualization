FROM ibm-semeru-runtimes:open-21-jre

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

ADD microservice-visualization/build/libs/microservice-visualization-*.jar app.jar

ENTRYPOINT java -jar app.jar
