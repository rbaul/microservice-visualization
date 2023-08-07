FROM ibm-semeru-runtimes:open-17-jre

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

ADD microservice-visualization/build/libs/microservice-visualization-*.jar app.jar

CMD java -jar app.jar
