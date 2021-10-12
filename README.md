[![codecov](https://codecov.io/gh/alwaysmartio/optimizer/branch/master/graph/badge.svg?token=QM96UTQZNZ)](https://codecov.io/gh/alwaysmartio/optimizer)

# mv-manager-service

Determines the most relevant materialized views in a given BigQuery context.

# Development

## With docker

Run Postgres and Adminer

```shell script
docker-compose -f stack.yml
```

## With AppEngine (local mode)

```shell script
mvn package appengine:run
```

# Tomcat

Maven dependency must have the provided scope in order to run the service as a Spring standalone
application (for development and debug) and let AppEngine provide his own servlet.

# Deploy on AppEngine

```shell script
# Set the your gcloud project id
gcloud config set project <project-id>
# Package with maven and deploy in appengine
mvn package appengine:deploy
```

