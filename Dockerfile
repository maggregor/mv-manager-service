FROM maven:3-openjdk-8

RUN mkdir -p /build
WORKDIR /build

COPY . /build

# Build application
RUN mvn -DskipTests clean install
RUN rm target/mvm-service-0.0.0-dev.war.original

ENTRYPOINT ["java", "-jar", "target/mvm-service-0.0.0-dev.war"]

