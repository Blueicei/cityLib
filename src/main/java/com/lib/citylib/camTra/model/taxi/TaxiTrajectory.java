package com.lib.citylib.camTra.model.taxi;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "出租车轨迹")
public class TaxiTrajectory {
    private String traId;
    private String carNumber;
    private Date startTime;
    private Date endTime;
    @ApiModelProperty(value = "载客里程(km)")
    private Double distanceCarry;
    @ApiModelProperty(value = "空驶里程(km)")
    private Double distanceEmpty;
    @ApiModelProperty(value = "计算得到的行驶距离(km)")
    private Double distanceCal;

    private Integer timeInterval;
    private Double avgSpeed;
    private List<GpsPoint> points = new ArrayList<>();
}
