package com.miradorstack.playground;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api")
@Tag(name = "Data Operations", description = "CRUD operations for key-value data with dual storage (Valkey + Cassandra)")
public class DataController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/read/{key}")
    @Operation(summary = "Read data from both storage systems",
               description = "Retrieves the value for a given key from both Valkey (cache) and Cassandra (persistent storage)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Key not found")
    })
    public ResponseEntity<Map<String, String>> read(
            @Parameter(description = "The key to retrieve", required = true, example = "mykey")
            @PathVariable String key) {
        Map<String, String> result = new HashMap<>();

        // Read from Valkey
        String valkeyValue = redisTemplate.opsForValue().get(key);
        result.put("valkey", valkeyValue != null ? valkeyValue : "Not found");

        // Simulate Cassandra read (for demo, using Redis with prefix)
        String cassandraValue = redisTemplate.opsForValue().get("cassandra_" + key);
        result.put("cassandra", cassandraValue != null ? cassandraValue : "Not found");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new key-value pair",
               description = "Creates a new key-value pair in both Valkey and Cassandra storage systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    public ResponseEntity<String> create(
            @Parameter(description = "The key for the new data", required = true, example = "mykey")
            @RequestParam String key,
            @Parameter(description = "The value to store", required = true, example = "myvalue")
            @RequestParam String value) {
        // Create in Valkey
        redisTemplate.opsForValue().set(key, value);

        // Simulate create in Cassandra
        redisTemplate.opsForValue().set("cassandra_" + key, value);

        return ResponseEntity.ok("Created");
    }

    @PutMapping("/modify/{key}")
    @Operation(summary = "Modify an existing key-value pair",
               description = "Updates the value for an existing key in both Valkey and Cassandra storage systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data modified successfully"),
        @ApiResponse(responseCode = "404", description = "Key not found")
    })
    public ResponseEntity<String> modify(
            @Parameter(description = "The key to modify", required = true, example = "mykey")
            @PathVariable String key,
            @Parameter(description = "The new value", required = true, example = "newvalue")
            @RequestParam String value) {
        // Check if exists in Valkey
        if (redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.opsForValue().set("cassandra_" + key, value);
            return ResponseEntity.ok("Modified");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{key}")
    @Operation(summary = "Delete a key-value pair",
               description = "Deletes the key-value pair from both Valkey and Cassandra storage systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Key not found")
    })
    public ResponseEntity<String> delete(
            @Parameter(description = "The key to delete", required = true, example = "mykey")
            @PathVariable String key) {
        // Delete from Valkey
        redisTemplate.delete(key);

        // Simulate delete from Cassandra
        redisTemplate.delete("cassandra_" + key);

        return ResponseEntity.ok("Deleted");
    }
}