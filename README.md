![Cloud Build](https://storage.googleapis.com/goptimal-badges/builds/optimizer/branches/master.svg)
[![codecov](https://codecov.io/gh/alwaysmartio/optimizer/branch/master/graph/badge.svg?token=QM96UTQZNZ)](https://codecov.io/gh/alwaysmartio/optimizer)

# Optimizer
Determines the most relevant materialized views in a given BigQuery context.

# Development
Run Postgres and Adminer
```shell script
docker-compose -f stack.yml
```
# Deploy
```shell script
# Set the your gcloud project id
gcloud config set project achilio-dev
# Deploy in appengine
mvn clean package appengine:deploy
```

