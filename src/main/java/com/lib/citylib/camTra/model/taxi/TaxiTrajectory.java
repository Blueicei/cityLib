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
    @Alias("tra_id")
    private String traId;
    @Alias("car_number")
    private String carNumber;
    @Alias("start_time")
    private Date startTime;
    @Alias("end_time")
    private Date endTime;
    @Alias("distance_carry")
    @ApiModelProperty(value = "载客里程(km)")
    private Double distanceCarry;
    @Alias("distance_empty")
    @ApiModelProperty(value = "空驶里程(km)")
    private Double distanceEmpty;
    @Alias("distance_cal")
    @ApiModelProperty(value = "计算得到的行驶距离(km)")

    private Double distanceCal;
    private Integer timeInterval;
    private Double avgSpeed;
    private List<GpsPoint> points = new ArrayList<>();
}
