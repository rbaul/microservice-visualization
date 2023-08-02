FROM ibm-semeru-runtimes:open-17-jre

# create folder in the container - can be useful to mount host filesystem into the container
RUN mkdir -p /app
WORKDIR /app

ADD https://github.com/rbaul/microservice-visualization/releases/download/0.0.1/microservice-visualization-0.0.1.jar microservice-visualization.jar
COPY ../result result
COPY application-projects.yaml application-projects.yaml

CMD java -jar microservice-visualization.jar
