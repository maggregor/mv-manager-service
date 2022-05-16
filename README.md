###### Main [![codecov](https://codecov.io/gh/achilio/mv-manager-service/branch/main/graph/badge.svg?token=SAABWG9HJO)](https://codecov.io/gh/achilio/mv-manager-service) | Dev [![codecov](https://codecov.io/gh/achilio/mv-manager-service/branch/dev/graph/badge.svg?token=SAABWG9HJO)](https://codecov.io/gh/achilio/mv-manager-service)

# mv-manager-service

Determines the most relevant materialized views in a given BigQuery context.

# Development

## With docker

Run Postgres and Adminer

```shell script
docker-compose up db adminer
```

# Run

```shell script
mvn spring-boot:run
```

Run Postgres, Adminer, and the web app locally

```shell script
docker-compose up
```

# Setup Google Pub/Sub Emulator
Clone and install the python-pubsub client: https://github.com/googleapis/python-pubsub
```
cd samples/snippets
$(gcloud beta emulators pubsub env-init)
python subscriber.py achilio-dev create sseTopic
python subscriber.py achilio-dev create-push sseTopic events http://localhost:8082/events
```
