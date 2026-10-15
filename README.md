# Dynamic JSON Operator

A Spring Boot REST application that inserts records into a MySQL dataset and queries them with dynamic **group-by** and **sort-by** operations driven by URL parameters.

## Live Links

| Item | URL |
|------|-----|
| Live application | `<add your deployed URL>` |
| Swagger UI | `<add your deployed URL>/swagger-ui/index.html` |
| Loom demo | `<add your Loom link>` |

## Tech Stack

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC, Spring JDBC)
- MySQL with `JdbcTemplate`
- springdoc-openapi (Swagger UI)
- Lombok, SLF4J logging
- Docker

## Architecture

```
Client (Postman / Swagger)
        |
DatasetController   (@RestController - request/response)
        |
DatasetService      (validation, grouping logic)
        |
DatasetRepo         (JdbcTemplate - SQL built with StringBuilder)
        |
MySQL               (employee_dataset table)
```

Project layout (`com.json.dynamic_json_operator`):

| Package | Purpose |
|---------|---------|
| `controller` | REST endpoints and Swagger annotations |
| `service` | Validation, whitelisting, grouping |
| `repo` | SQL execution with `JdbcTemplate` |
| `model` | `DatasetModel` (id, name, age, department) |
| `exception` | `DatasetException` and the global exception handler |
| `config` | OpenAPI configuration |

## Database

Database: `datasets`, table: `employee_dataset`

| Column | Type | Notes |
|--------|------|-------|
| id | INT | Primary key |
| name | VARCHAR(100) | |
| age | INT | |
| department | VARCHAR(100) | |

The table and 10 sample rows are created automatically on startup from `src/main/resources/schema.sql` (`CREATE TABLE IF NOT EXISTS` and `INSERT IGNORE`, so restarts are safe).

## Running Locally

**Prerequisites:** JDK 21, MySQL running locally.

1. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS datasets;
   ```
2. Create `src/main/resources/application-local.properties` (this file is git-ignored):
   ```properties
   spring.datasource.password=your_mysql_password
   ```
3. Run the app with the `local` profile.
   - Command line:
     ```bash
     ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
     ```
     On Windows PowerShell:
     ```powershell
     .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
     ```
   - Eclipse: add the VM argument `-Dspring.profiles.active=local` to the Run Configuration.
4. Open Swagger UI: http://localhost:8080/swagger-ui/index.html

### Configuration (environment variables)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/datasets` | JDBC URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | _(empty)_ | Database password |
| `PORT` | `8080` | Server port |
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |

## Running with Docker

```bash
docker build -t dynamic-json-operator .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://<host>:3306/datasets \
  -e DB_USERNAME=<user> \
  -e DB_PASSWORD=<password> \
  dynamic-json-operator
```

## API Documentation

Interactive documentation is available in Swagger UI at `/swagger-ui/index.html`.

### 1. Insert Record

`POST /api/dataset/{datasetName}/record`

Inserts a record into the given dataset. Supported dataset: `employee_dataset`.

**Request body**

```json
{
  "id": 11,
  "name": "Ian Moore",
  "age": 31,
  "department": "Sales"
}
```

**Response** (`201 Created`)

```json
{
  "message": "Record added successfully",
  "dataset": "employee_dataset",
  "recordId": 11
}
```

**curl**

```bash
curl -X POST http://localhost:8080/api/dataset/employee_dataset/record \
  -H "Content-Type: application/json" \
  -d '{"id":11,"name":"Ian Moore","age":31,"department":"Sales"}'
```

**Validation rules:** `id` and `age` must be present and greater than 0; `name` and `department` must not be blank.

### 2. Query Records (Group-By and Sort-By)

`GET /api/dataset/{datasetName}/query`

| Parameter | Required | Description |
|-----------|----------|-------------|
| `groupBy` | No | Column to group by: `id`, `name`, `age`, `department` |
| `sortBy` | No | Column to sort by: `id`, `name`, `age`, `department` |
| `order` | No | `asc` (default) or `desc` |

The SQL `ORDER BY` clause is built dynamically from these parameters.

| Parameters | Generated SQL (after `SELECT id, name, age, department FROM employee_dataset`) | Response key |
|------------|-----------------|--------------|
| none | _(no ORDER BY)_ | `records` |
| `sortBy=age&order=desc` | `ORDER BY age DESC` | `sortedRecords` |
| `groupBy=department` | `ORDER BY department ASC` | `groupedRecords` |
| `groupBy=department&sortBy=age&order=asc` | `ORDER BY department ASC, age ASC` | `groupedRecords` |

When both are given, records inside each group are sorted by `sortBy`.

**Example: group by**

```bash
curl "http://localhost:8080/api/dataset/employee_dataset/query?groupBy=department"
```

```json
{
  "groupedRecords": {
    "Engineering": [
      { "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" },
      { "id": 2, "name": "Jane Smith", "age": 25, "department": "Engineering" }
    ],
    "HR": [
      { "id": 7, "name": "Emma Davis", "age": 33, "department": "HR" }
    ]
  }
}
```

**Example: sort by**

```bash
curl "http://localhost:8080/api/dataset/employee_dataset/query?sortBy=age&order=desc"
```

```json
{
  "sortedRecords": [
    { "id": 8, "name": "Frank Miller", "age": 45, "department": "Sales" },
    { "id": 5, "name": "Carol White", "age": 41, "department": "Engineering" }
  ]
}
```

### Error Responses

All errors return `{"error": "<message>"}`.

| Status | When |
|--------|------|
| 400 | Invalid JSON body, missing/invalid field, invalid `groupBy`/`sortBy` column, or `order` not `asc`/`desc` |
| 404 | Unknown dataset name |
| 409 | A record with the same `id` already exists |
| 500 | Unexpected server error (details are only written to the log) |

## Design Notes and Assumptions

- **SQL injection protection:** table and column names cannot be bound as `?` parameters, so the table name and the `groupBy`/`sortBy` columns are checked against hardcoded whitelists (`employee_dataset`; `id`, `name`, `age`, `department`) before the SQL is built with `StringBuilder`. All record values are inserted through bound parameters.
- **Supported dataset:** one pre-created table, `employee_dataset`. Any other dataset name returns 404.
- **Grouping:** SQL orders the rows by the group column, and the service then splits the ordered rows into groups in Java, so full records are returned inside each group while keeping the sort order.
- **Secrets:** no credentials are committed. The database password is supplied through `DB_PASSWORD` or a git-ignored `application-local.properties`.
- **Logging:** SLF4J (`@Slf4j`) logs requests at `INFO`, and SQL statements at `DEBUG` when enabled with `LOG_LEVEL=DEBUG`.
