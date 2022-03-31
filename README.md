###### Main [![codecov](https://codecov.io/gh/achilio/mv-manager-service/branch/main/graph/badge.svg?token=SAABWG9HJO)](https://codecov.io/gh/achilio/mv-manager-service) | Dev [![codecov](https://codecov.io/gh/achilio/mv-manager-service/branch/dev/graph/badge.svg?token=SAABWG9HJO)](https://codecov.io/gh/achilio/mv-manager-service)

# mv-manager-service

Determines the most relevant materialized views in a given BigQuery context.

# Development

## With docker

Run Postgres and Adminer

```shell script
docker-compose up db adminer
```

# Tomcat

Maven dependency must have the provided scope in order to run the service as a Spring standalone
application (for development and debug) and let AppEngine provide his own servlet.

# Run

```shell script
mvn spring-boot:run
```

Run Postgres, Adminer, and the web app locally

```shell script
docker-compose up
```