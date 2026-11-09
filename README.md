# LinkPulse

A small link-shortening and click-analytics system built as a multi-module Spring Boot (Gradle) project.

- **shortener-service**: creates short links, redirects, and publishes a click event to Kafka for each redirect.
- **analytics-service**: consumes click events from Kafka, stores them in PostgreSQL, and serves statistics over a REST API.
- **common**: shared code used by both services.

```
Client ──► shortener-service ──► Kafka (topic: click-events) ──► analytics-service
                 │                                                      │
                 └────────────────────► PostgreSQL ◄────────────────────┘
```

## Tech stack

Java 21 · Spring Boot · Gradle (multi-module) · PostgreSQL 16 · Flyway · Apache Kafka · Docker · Testcontainers · GitHub Actions

## Prerequisites

- JDK 21
- Docker (Docker Desktop, or Colima on macOS)
- Git

Check Docker is running before anything else:

```bash
docker info
```

## Quick start (services run on your machine, infrastructure in Docker)

**1. Start PostgreSQL and Kafka**

```bash
docker compose up -d postgres kafka
docker compose ps
```

**2. Check the Kafka topic exists**

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

You should see `click-events`. If it is missing, create it:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --create \
  --topic click-events --partitions 1 --replication-factor 1
```

**3. Run the services** (one terminal each)

```bash
./gradlew :shortener-service:bootRun
./gradlew :analytics-service:bootRun
```

Flyway applies the database migrations automatically on startup, and Hibernate runs with `ddl-auto: validate` to check that the entities match the schema.

**4. Try it**

<!-- TODO: confirm these paths and the request body against the controllers (grep -rn "Mapping" --include=*.java .) -->

*shortener-service* (port 8080)

```bash
# create a short link
curl -s -X POST http://localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com"}'

# look up a short link (records a click and publishes an event to Kafka)
curl -i http://localhost:8080/api/links/<short-code>
```

The first call returns the generated short code. Each call to the second one produces one click event.

*analytics-service* (port 8081)

```bash
# statistics for a short code
curl -s http://localhost:8081/api/stats/<short-code>
```

Click the short link a few times, then call the stats endpoint again and the numbers should go up.

| Service | Method | Path | Purpose |
|---|---|---|---|
| shortener-service | `POST` | `/api/links` | Create a short link |
| shortener-service | `GET` | `/api/links/{shortCode}` | Look up a short link and publish a click event |
| analytics-service | `GET` | `/api/stats/{shortCode}` | Click statistics for a short code |

**5. Confirm the click landed in the database**

```bash
docker compose exec postgres psql -U linkpulse -d linkpulse \
  -c 'SELECT count(*) FROM click_records;'
```

## Monitoring Kafka

These commands run inside the Kafka container, so they use `localhost:9092` and the full path to the scripts (`/opt/kafka/bin/`).

**Read every click event from the beginning of the topic** (press `Ctrl+C` to stop)

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic click-events \
  --from-beginning
```

Show the message key and timestamp as well:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic click-events \
  --from-beginning \
  --property print.key=true \
  --property print.timestamp=true
```

Read only the first 10 events and exit:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic click-events \
  --from-beginning \
  --max-messages 10
```

**Watch new events live** (leave out `--from-beginning`, then click a short link in another terminal)

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic click-events
```

The console consumer uses its own temporary consumer group, so it does not take messages away from `analytics-service`.

**Check that analytics-service is keeping up**

```bash
# list consumer groups, then describe the analytics one
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list

docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group <analytics-group-id>
```

`LAG` is the number of events not yet consumed. It should be `0` once the analytics service has caught up.

**Describe the topic** (partitions and leader)

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe --topic click-events
```

## Running the services in Docker

Each service has a multi-stage `Dockerfile`. Build from the **repository root**, because the multi-module build needs the root `settings.gradle` and `common/`.

**Build the images**

```bash
docker build -f shortener-service/Dockerfile -t linkpulse-shortener .
docker build -f analytics-service/Dockerfile -t linkpulse-analytics .
```

**Find the compose network** (usually `linkpulse_default`)

```bash
docker network ls
```

**Run the containers** (one terminal each). `SERVER_PORT` pins the port inside the container so the `-p` mapping always matches.

```bash
docker run --rm --name linkpulse-shortener -p 8080:8080 \
  --network linkpulse_default \
  -e SERVER_PORT=8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/linkpulse \
  -e SPRING_DATASOURCE_USERNAME=linkpulse \
  -e SPRING_DATASOURCE_PASSWORD=linkpulse \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  linkpulse-shortener
```

```bash
docker run --rm --name linkpulse-analytics -p 8081:8081 \
  --network linkpulse_default \
  -e SERVER_PORT=8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/linkpulse \
  -e SPRING_DATASOURCE_USERNAME=linkpulse \
  -e SPRING_DATASOURCE_PASSWORD=linkpulse \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  linkpulse-analytics
```

Do not run the same service from Gradle and Docker at the same time. Two analytics consumers in one consumer group split the topic's partitions between them.

### Kafka listeners

Kafka advertises two addresses, and the one you use depends on where the client runs:

| Client runs | Bootstrap address |
|---|---|
| On your machine (`bootRun`, IDE) | `localhost:9092` |
| In a container on the compose network | `kafka:29092` |

A container that connects to `localhost:9092` fails with connection timeouts, because inside the container `localhost` is the container itself.

## Database

- PostgreSQL 16, database `linkpulse`, user `linkpulse`, password `linkpulse` (local development only).
- Schema is versioned with **Flyway**. Each service keeps its migrations in `src/main/resources/db/migration`.
- `spring.jpa.hibernate.ddl-auto` is `validate`: Hibernate only checks the schema and never changes it. Schema changes always go in a new migration file. Do not edit a migration that has already been applied.

### Query performance

The analytics queries (clicks per short code, clicks per day for the last 30 days) are served by a composite index on `click_records (short_code, clicked_at)`: equality column first, range column second. Measured with `EXPLAIN (ANALYZE, BUFFERS)` on 1M rows:

| Query | Before (seq scan) | After (index-only scan) |
|---|---|---|
| Count per short code | ~30.0 ms | ~0.42 ms |
| Daily clicks, last 30 days | ~17.2 ms | ~0.35 ms |

To reproduce, seed rows in `psql`, run `ANALYZE click_records;`, and compare the plans with and without the index.

## Tests

```bash
./gradlew build
```

The integration tests start a throwaway PostgreSQL container with **Testcontainers**, run the Flyway migrations against it, and exercise the real repository queries. They do not use the database from `docker compose`, so they behave the same on your machine and in CI. Docker must be running.

If you use Colima on macOS:

```bash
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

## CI/CD

`.github/workflows/ci.yml` runs on every push to `main` and on pull requests:

1. **build**: sets up Java 21, runs `./gradlew build` (compile and all tests), uploads the test reports.
2. **publish**: only on pushes to `main`, after `build` passes. Builds both service images and pushes them to GitHub Container Registry, tagged `latest` and with the commit SHA.

```
ghcr.io/<owner>/linkpulse-shortener-service
ghcr.io/<owner>/linkpulse-analytics-service
```

## Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| `Schema validation: missing table` | The migrations did not run. Check that Flyway is enabled and the file is `db/migration/V1__*.sql` (capital V, two underscores). |
| `Found non-empty schema(s) "public" but no schema history table` | Two services share one database and the second sees tables from the first. Set `spring.flyway.baseline-on-migrate: true` with `baseline-version: 0` for that service. |
| Container logs show `Connection to node ... (localhost/127.0.0.1:9092)` | The container is using the host Kafka address. Use `kafka:29092` and run on the compose network. |
| `curl: (56) Recv failure: Connection reset by peer` | Nothing is listening on the container port you mapped. Set `SERVER_PORT` and make the right side of `-p` match it. |
| Tests fail with `ConnectException` on CI only | A test depends on a locally running database. Give it its own Testcontainers Postgres. |
| `docker compose exec` says the service is not running | Use the **service** name (`kafka`, `postgres`), not the container name (`linkpulse-kafka`). |

## Stopping everything

```bash
docker compose down        # stop containers, keep data
docker compose down -v     # stop containers and delete the database volume
```