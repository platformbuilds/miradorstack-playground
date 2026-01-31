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
#### Execution Flow (Step-by-Step)
1. Client sends GET request to `/api/read/{key}`
2. `DataController.read(key)` is invoked
3. Read value from Valkey using `redisTemplate.opsForValue().get(key)`
4. Read value from Cassandra using `cassandraOperations.selectOneById(key, KeyValue.class)`
5. Handle results and service unavailability for both stores
6. Build response map with results from Valkey and Cassandra
7. Return `ResponseEntity<Map<String, String>>` to client
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
#### Execution Flow (Step-by-Step)
1. Client sends GET request to `/api/list`
2. `DataController.list()` is invoked
3. Fetch all keys from Valkey using `redisTemplate.keys("*")`
4. Convert the set of keys to a list
5. Return `ResponseEntity<List<String>>` to client
#### Implementation Details
- **Class:** `DataController`
- **Method:** `list()`
    - Fetches all keys from Valkey using `redisTemplate.keys("*")`.
    - Returns as a list.
    - Handles Valkey service unavailability.

---

### Create Key-Value (`POST /api/create`)
#### Execution Flow (Step-by-Step)
1. Client sends POST request to `/api/create` with `key` and `value` parameters
2. `DataController.create(key, value)` is invoked
3. Store key-value in Valkey using `redisTemplate.opsForValue().set(key, value)`
4. Store key-value in Cassandra using `cassandraOperations.insert(new KeyValue(key, value))`
5. Handle results and service unavailability for both stores
6. Return `ResponseEntity<String>` ("Created") to client
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
#### Execution Flow (Step-by-Step)
1. Client sends PUT request to `/api/modify/{key}` with `value` parameter
2. `DataController.modify(key, value)` is invoked
3. Check if key exists in Valkey using `redisTemplate.hasKey(key)`
4. If key exists:
    - Update value in Valkey using `redisTemplate.opsForValue().set(key, value)`
    - Update value in Cassandra using `cassandraOperations.update(new KeyValue(key, value))`
    - Return `ResponseEntity<String>` ("Modified")
5. If key does not exist:
    - Return 404 Not Found
#### Implementation Details
- **Class:** `DataController`
- **Method:** `modify(String key, String value)`
    - Checks existence in Valkey.
    - Updates value in Valkey and Cassandra if present.
    - Returns "Modified" or 404 if not found.

---

### Delete Key-Value (`DELETE /api/delete/{key}`)
#### Execution Flow (Step-by-Step)
1. Client sends DELETE request to `/api/delete/{key}`
2. `DataController.delete(key)` is invoked
3. Delete key from Valkey using `redisTemplate.delete(key)`
4. Delete key from Cassandra using `cassandraOperations.deleteById(key, KeyValue.class)`
5. Handle results and service unavailability for both stores
6. Return `ResponseEntity<String>` ("Deleted") to client
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
