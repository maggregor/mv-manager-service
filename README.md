![Cloud Build](https://storage.googleapis.com/goptimal-badges/builds/optimizer/branches/master.svg)
[![codecov](https://codecov.io/gh/alwaysmartio/optimizer/branch/master/graph/badge.svg?token=QM96UTQZNZ)](https://codecov.io/gh/alwaysmartio/optimizer)

# Optimizer
Determines the most relevant materialized views in a given BigQuery context.

# Development
Run Postgres and Adminer
```shell script
docker-compose -f stack.yml
```

# HTTP Routes
- GET /api/v1/project
- GET /api/v1/project/{projectName}
- GET /api/v1/dataset/{projectName.datasetName}
- GET /api/v1/table/{projectName.datasetName.tableName}
