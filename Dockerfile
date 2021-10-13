FROM maven:3-jdk-8

RUN apt update
RUN apt install wget unzip -y

RUN mkdir -p /build
WORKDIR /build

COPY . /build

# Build application
RUN mvn -DskipTests clean install
ENTRYPOINT ["mvn", "spring-boot:run"]

