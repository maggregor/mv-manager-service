[![codecov](https://codecov.io/gh/achilio/mv-manager-service/branch/master/graph/badge.svg?token=SAABWG9HJO)](https://codecov.io/gh/achilio/mv-manager-service)

# mv-manager-service

Determines the most relevant materialized views in a given BigQuery context.

# Development

## With docker

Run Postgres and Adminer

```shell script
docker-compose up db adminer
```

Run Postgres, Adminer, and the web app locally

```shell script
docker-compose up
```

## With AppEngine (local mode)

```shell script
mvn package appengine:run
```

# Tomcat

Maven dependency must have the provided scope in order to run the service as a Spring standalone
application (for development and debug) and let AppEngine provide his own servlet.

# Run

```shell script
mvn spring-boot:run
```

# Deploy on AppEngine

```shell script
# Set the your gcloud project id
gcloud config set project <project-id>
# Package with maven and deploy in appengine
mvn package appengine:deploy
```

