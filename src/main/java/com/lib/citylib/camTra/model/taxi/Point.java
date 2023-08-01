package com.lib.citylib.camTra.model.taxi;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "位置")
public class Point {
    private Double lng;
    private Double lat;
    private int count;
}
