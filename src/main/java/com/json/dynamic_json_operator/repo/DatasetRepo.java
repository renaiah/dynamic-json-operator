package com.json.dynamic_json_operator.repo;

import com.json.dynamic_json_operator.model.DatasetModel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetRepo {

    private final JdbcTemplate jdbcTemplate;

    public DatasetRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private static final Logger log = LoggerFactory.getLogger(DatasetRepo.class);

    public int insert(String tableName, DatasetModel m) {
        StringBuilder sql = new StringBuilder("INSERT INTO `")
                .append(tableName)
                .append("` (id, name, age, department) VALUES (?, ?, ?, ?)");
        log.debug("sql query is :{}",sql.toString());
        return jdbcTemplate.update(sql.toString(),
                m.getId(), m.getName(), m.getAge(), m.getDepartment());
    }
    
    public List<DatasetModel> findAll(String tableName, String groupBy, String sortBy, String order) {
        StringBuilder sql = new StringBuilder("SELECT id, name, age, department FROM `")
                .append(tableName).append("`");

        List<String> orderParts = new ArrayList<>();
        if (groupBy != null) {
              orderParts.add(groupBy + " "+ order.toLowerCase());            
        }
        if (sortBy != null) {
            orderParts.add(sortBy + " " + order.toUpperCase());
        }
        if (!orderParts.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderParts));
        }
        log.debug("sql query :{}",sql.toString());
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(DatasetModel.class));
    }
}