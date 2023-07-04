package com.lib.citylib.camTra.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableProcessDto {

    private String tableName;
    private List<String> carNumber;
    private List<String> carType;
    private Long traCut;
    private Long pointNumber;
    private Long traLength;
    private Boolean filterTraRange;
    private List<String> traRange;
}
