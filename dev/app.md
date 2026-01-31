# MiradorStack Playground API – Deep Dive Engineering Document

---

## Table of Contents
1. [Overview](#overview)
2. [API Deep Dive](#api-deep-dive)
    - [Read Key (`GET /api/read/{key}`)](#read-key-get-apireadkey)
    - [List Keys (`GET /api/list`)](#list-keys-get-apilist)
    - [Create Key-Value (`POST /api/create`)](#create-key-value-post-apicreate)
    - [Modify Key-Value (`PUT /api/modify/{key}`)](#modify-key-value-put-apimodifykey)
    - [Delete Key-Value (`DELETE /api/delete/{key}`)](#delete-key-value-delete-apideletekey)
3. [Class Implementation Details](#class-implementation-details)

---

## Overview
This document provides a deep technical dive into the engineering implementation of each API in the MiradorStack Playground application. For each API, a flow chart illustrates the execution path, and all classes and functions involved are described in detail.

---

## API Deep Dive

### Read Key (`GET /api/read/{key}`)
#### Flow Chart
```mermaid
graph TD
    A[Client Request: GET /api/read/{key}] --> B[DataController.read(key)]
    B --> C[Read from Valkey (redisTemplate.opsForValue().get)]
    B --> D[Read from Cassandra (cassandraOperations.selectOneById)]
    C --> E[Handle Valkey result]
    D --> F[Handle Cassandra result]
    E --> G[Build response map]
    F --> G
    G --> H[Return ResponseEntity<Map<String, String>>]
```
#### Implementation Details
- **Class:** `DataController`
- **Method:** `read(String key)`
    - Reads value from Valkey (Redis) and Cassandra.
    - Handles service unavailability and missing keys.
    - Returns a map with results from both stores.
- **Supporting Classes:**
    - `RedisTemplate<String, String>`: For Valkey operations.
    - `CassandraOperations`: For Cassandra operations.
    - `KeyValue`: Data model for Cassandra.

---

### List Keys (`GET /api/list`)
#### Flow Chart
```mermaid
graph TD
    A[Client Request: GET /api/list] --> B[DataController.list()]
    B --> C[Fetch all keys from Valkey (redisTemplate.keys)]
    C --> D[Convert Set to List]
    D --> E[Return ResponseEntity<List<String>>]
```
#### Implementation Details
- **Class:** `DataController`
- **Method:** `list()`
    - Fetches all keys from Valkey using `redisTemplate.keys("*")`.
    - Returns as a list.
    - Handles Valkey service unavailability.

---

### Create Key-Value (`POST /api/create`)
#### Flow Chart
```mermaid
graph TD
    A[Client Request: POST /api/create] --> B[DataController.create(key, value)]
    B --> C[Store in Valkey (redisTemplate.opsForValue().set)]
    B --> D[Store in Cassandra (cassandraOperations.insert)]
    C --> E[Handle Valkey result]
    D --> F[Handle Cassandra result]
    E --> G[Return ResponseEntity<String>]
    F --> G
```
#### Implementation Details
- **Class:** `DataController`
- **Method:** `create(String key, String value)`
    - Stores key-value in Valkey and Cassandra.
    - Handles service unavailability.
    - Returns "Created".
- **Supporting Classes:**
    - `KeyValue`: Used for Cassandra insert.

---

### Modify Key-Value (`PUT /api/modify/{key}`)
#### Flow Chart
```mermaid
graph TD
    A[Client Request: PUT /api/modify/{key}] --> B[DataController.modify(key, value)]
    B --> C[Check if key exists in Valkey (redisTemplate.hasKey)]
    C -->|Exists| D[Update in Valkey (redisTemplate.opsForValue().set)]
    D --> E[Update in Cassandra (cassandraOperations.update)]
    E --> F[Return ResponseEntity<String>]
    C -->|Not Exists| G[Return 404]
```
#### Implementation Details
- **Class:** `DataController`
- **Method:** `modify(String key, String value)`
    - Checks existence in Valkey.
    - Updates value in Valkey and Cassandra if present.
    - Returns "Modified" or 404 if not found.

---

### Delete Key-Value (`DELETE /api/delete/{key}`)
#### Flow Chart
```mermaid
graph TD
    A[Client Request: DELETE /api/delete/{key}] --> B[DataController.delete(key)]
    B --> C[Delete from Valkey (redisTemplate.delete)]
    B --> D[Delete from Cassandra (cassandraOperations.deleteById)]
    C --> E[Handle Valkey result]
    D --> F[Handle Cassandra result]
    E --> G[Return ResponseEntity<String>]
    F --> G
```
#### Implementation Details
- **Class:** `DataController`
- **Method:** `delete(String key)`
    - Deletes key from Valkey and Cassandra.
    - Handles service unavailability.
    - Returns "Deleted".

---

## Class Implementation Details

### DataController
- **Annotations:**
    - `@RestController`, `@RequestMapping("/api")`, `@Tag`
- **Dependencies:**
    - `RedisTemplate<String, String>`: Autowired for Valkey operations.
    - `CassandraOperations`: Autowired for Cassandra operations.
- **Logging:**
    - Uses SLF4J `Logger` for warnings/errors.
- **API Methods:**
    - `read(String key)`: Read from both stores.
    - `list()`: List all keys from Valkey.
    - `create(String key, String value)`: Create in both stores.
    - `modify(String key, String value)`: Update in both stores.
    - `delete(String key)`: Delete from both stores.

### KeyValue
- **Annotations:**
    - `@Table("key_value")`, `@PrimaryKey`
- **Fields:**
    - `key`: String (Primary Key)
    - `value`: String
- **Usage:**
    - Used for Cassandra persistence.

### OpenApiConfig
- **Annotations:**
    - `@Configuration`, `@Bean`
- **Purpose:**
    - Configures OpenAPI/Swagger documentation.

### PlaygroundApplication
- **Annotations:**
    - `@SpringBootApplication(exclude = {CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class})`
- **Purpose:**
    - Main entry point for Spring Boot application.

### RootRedirectController
- **Annotations:**
    - `@Controller`
- **Purpose:**
    - Redirects root path `/` to Swagger UI.

---

## Notes
- All error handling is done via logging and fallback responses.
- Dual storage ensures both cache and persistence for all operations.
- API documentation is auto-generated and accessible via Swagger UI.

---

*This document is strictly based on the actual source code. No assumptions or guesses have been made.*
