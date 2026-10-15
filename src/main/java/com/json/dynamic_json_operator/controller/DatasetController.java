package com.json.dynamic_json_operator.controller;

import com.json.dynamic_json_operator.model.DatasetModel;
import com.json.dynamic_json_operator.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
@Slf4j
@Tag(name = "Dataset", description = "Insert records and query them with group-by and sort-by")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }
    
    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    @Operation(summary = "Insert a record into a dataset",
               description = "Persists one record in the given dataset table (supported dataset: employee_dataset).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Record added successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed or invalid JSON"),
        @ApiResponse(responseCode = "404", description = "Dataset not found"),
        @ApiResponse(responseCode = "409", description = "A record with this id already exists")
    })
    @PostMapping("/{datasetName}/record")
    public ResponseEntity<Map<String, Object>> insertRecord(
            @Parameter(description = "Dataset name", example = "employee_dataset")
            @PathVariable String datasetName,
            @RequestBody DatasetModel record) {

        log.info("Insert request on dataset {}", datasetName);
        int recordId = service.insertRecord(datasetName, record);
        log.info("RecordId {} inserted successfully in dataset {}", recordId, datasetName);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Record added successfully");
        response.put("dataset", datasetName);
        response.put("recordId", recordId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Query a dataset with group-by and sort-by",
               description = "All parameters are optional. With groupBy the response is groupedRecords; "
                           + "with only sortBy it is sortedRecords; with neither it is records. "
                           + "When both are given, records inside each group are sorted.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Query result"),
        @ApiResponse(responseCode = "400", description = "Invalid column or order"),
        @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/{datasetName}/query")
    public Map<String, Object> query(
            @Parameter(description = "Dataset name", example = "employee_dataset")
            @PathVariable String datasetName,
            @Parameter(description = "Column to group by",
                       schema = @Schema(allowableValues = {"id", "name", "age", "department"}))
            @RequestParam(required = false) String groupBy,
            @Parameter(description = "Column to sort by",
                       schema = @Schema(allowableValues = {"id", "name", "age", "department"}))
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction",
                       schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(required = false, defaultValue = "asc") String order) {

        log.info("Query on {}: groupBy={}, sortBy={}, order={}", datasetName, groupBy, sortBy, order);
        Map<String, Object> result = service.query(datasetName, groupBy, sortBy, order);
        log.info("Query completed on dataset {}", datasetName);
        return result;
    }
}