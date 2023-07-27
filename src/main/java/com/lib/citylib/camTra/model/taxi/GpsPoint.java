package com.lib.citylib.camTra.model.taxi;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "gps轨迹点")
public class GpsPoint {
    private String carNumber;
    private Double lng;
    private Double lat;
    private Date time;
    private String traId;
}
