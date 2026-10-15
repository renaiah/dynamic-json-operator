package com.json.dynamic_json_operator.service;

import com.json.dynamic_json_operator.exception.DatasetException;
import com.json.dynamic_json_operator.model.DatasetModel;
import com.json.dynamic_json_operator.repo.DatasetRepo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DatasetService {

    private static final Set<String> ALLOWED_TABLES  = Set.of("employee_dataset");
    private static final Set<String> ALLOWED_COLUMNS = Set.of("id", "name", "age", "department");

    private final DatasetRepo repo;
    public DatasetService(DatasetRepo repo) {
        this.repo = repo;
    }
    
    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    public int insertRecord(String datasetName, DatasetModel record) {
        validateTable(datasetName);

        if (record.getId() <= 0) {
        	log.info("id is required and must be greater than 0");
            throw new DatasetException("id is required and must be greater than 0", HttpStatus.BAD_REQUEST);
        }
        if (record.getName() == null || record.getName().isBlank()) {
        	log.error("name is required");
            throw new DatasetException("name is required", HttpStatus.BAD_REQUEST);
        }
        if (record.getAge() <=0 || record.getAge() > 100) {
        	log.error("age is required and must be greater than 0 and less than 100");
            throw new DatasetException("age is required and must be greater than 0 and less than 100", HttpStatus.BAD_REQUEST);
        }
        if (record.getAge() < 18 || record.getAge() > 65) {
        	log.error("Employee age is between 18 to 65 only considered");
            throw new DatasetException("Employee age is between 18 to 65 only considered", HttpStatus.BAD_REQUEST);
        }
        if (record.getDepartment() == null || record.getDepartment().isBlank()) {
        	log.error("department is required");
            throw new DatasetException("department is required", HttpStatus.BAD_REQUEST);
        }
        repo.insert(datasetName, record);
        return record.getId();
    }

    public Map<String, Object> query(String datasetName, String groupBy, String sortBy, String order) {
        validateTable(datasetName);
        
        boolean hasGroup = groupBy != null && !groupBy.isBlank();
        boolean hasSort  = sortBy != null && !sortBy.isBlank();

        if (hasGroup) validateColumn(groupBy, "groupBy");
        if (hasSort)  validateColumn(sortBy, "sortBy");

        String dir = (order == null || order.isBlank()) ? "asc" : order.trim().toLowerCase();
        if (!dir.equals("asc") && !dir.equals("desc")) {
        	log.error("order must be 'ase' or 'desc'");
            throw new DatasetException("order must be 'asc' or 'desc'", HttpStatus.BAD_REQUEST);
        }

        List<DatasetModel> rows = repo.findAll(
                datasetName,
                hasGroup ? groupBy.trim() : null,
                hasSort ? sortBy.trim() : null,
                dir);
        log.info("{} rows fetched from dataset",rows.size());
        Map<String, Object> response = new LinkedHashMap<>();

        if (hasGroup) {
            Map<String, List<DatasetModel>> groups = new LinkedHashMap<>();
            for (DatasetModel row : rows) {
                String key = groupKey(row, groupBy.trim());
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }
            response.put("groupedRecords", groups);
        } else if (hasSort) {
            response.put("sortedRecords", rows);
        } else {
            response.put("records", rows);
        }
        return response;
    }

    private void validateTable(String datasetName) {
        if (datasetName == null || !ALLOWED_TABLES.contains(datasetName)) {
        	log.error("Dataset not found : {}",datasetName);
            throw new DatasetException("Dataset not found: " + datasetName, HttpStatus.NOT_FOUND);
        }
    }

    private void validateColumn(String column, String paramName) {
        if (!ALLOWED_COLUMNS.contains(column.trim())) {
        	log.error("Invalid {} column: {}.Allowed {}",paramName,column,ALLOWED_COLUMNS);
            throw new DatasetException(
                    "Invalid " + paramName + " column: " + column + ". Allowed: " + ALLOWED_COLUMNS,
                    HttpStatus.BAD_REQUEST);
        }
    }

    private String groupKey(DatasetModel row, String column) {
        return switch (column) {
            case "id"         -> String.valueOf(row.getId());
            case "name"       -> String.valueOf(row.getName());
            case "age"        -> String.valueOf(row.getAge());
            case "department" -> String.valueOf(row.getDepartment());
            default           -> throw new DatasetException("Invalid column", HttpStatus.BAD_REQUEST);
        };
    }
}