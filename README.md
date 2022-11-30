# quarkus-liquibase-with-runtime-datasources
Explore Quarkus Liquibase with Runtime configuration of datasources

This is:
1. A copy of the [liquibase-quickstart directory](https://github.com/quarkusio/quarkus-quickstarts/tree/main/liquibase-quickstart) in the [Quarkus Quickstarts](https://github.com/quarkusio/quarkus-quickstarts) repository, which follows the [Using Liquibase Guide](https://quarkus.io/guides/liquibase).  Modified to use PostgreSQL instead of H2.
2. With the [Custom ConfigSource Example](https://quarkus.io/guides/config-extending-support#example) added.
3. Modified to have [multiple datasources](https://quarkus.io/guides/datasource#multiple-datasources), and read the list of datasources from an environment variable at runtime.

The purpose is to explore whether Quarkus datasources+liquibase can be used when the list of data sources is known only at runtime, and **not** at build time.  For example: One data source per-tenant in a multi-tenant application.  The Development, Testing, Staging, and Production environments will all have different datasources.

From the [Quarkus datasource guide under "Configuring Multiple Datasources"](https://quarkus.io/guides/datasource#configuring-multiple-datasources):

> Even when only one database extension is installed, named databases need to specify at least one build time property so that Quarkus knows they exist.

While that is true under the prod profile, I have found that runtime-only datasource properties work under the dev profile.

This is a minimal reproducible example.  There's likely a good reason why runtime-only datasource properties work under the dev profile.  But my goal is to find a work-around so data sources specified at runtime-only trigger Liquibase migration, even if it doesn't use Quarkus datasources.

The data sources are specified comma-separated in the ```DB_SCHEMAS``` environment variable.  The data sources used in this example:

```bash
DB_SCHEMAS=tenant1,tenant2
```

## PostgreSQL DB
Start the DB in a Docker container:

```bash
docker run -d --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:14.1 
```

Create the DB schemas, or clean them between test runs:

```bash
PGPASSWORD=quarkus_test psql -h localhost -p 5432 -U quarkus_test -d quarkus_test --command="DROP SCHEMA IF EXISTS tenant1 CASCADE; CREATE SCHEMA IF NOT EXISTS tenant1; DROP SCHEMA IF EXISTS tenant2 CASCADE; CREATE SCHEMA IF NOT EXISTS tenant2;"
```

Check whether Liquibase migration has occurred:

```bash
PGPASSWORD=quarkus_test psql -h localhost -p 5432 -U quarkus_test -d quarkus_test --command="SELECT table_schema,table_name FROM information_schema.tables WHERE table_schema ~ 'tenant';"
```

```6 rows``` means Liquibase migration has occurred, and ```0 rows``` means it has not.

## Build and run under dev profile

```bash
DB_SCHEMAS=tenant1,tenant2 mvn quarkus:dev
```

## Build and run under prod profile

```bash
mvn package
DB_SCHEMAS=tenant1,tenant2 java -jar target/quarkus-app/quarkus-run.jar
```



