package com.json.dynamic_json_operator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "A record of the employee_dataset")
public class DatasetModel {
    @Schema(example = "11") private Integer id;
    @Schema(example = "Ian Moore") private String name;
    @Schema(example = "31") private Integer age;
    @Schema(example = "Sales") private String department;
}