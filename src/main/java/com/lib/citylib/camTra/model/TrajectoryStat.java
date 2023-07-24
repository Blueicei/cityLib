package com.lib.citylib.camTra.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "CarTrajectory",description = "这个类定义了轨迹的所有属性")
@Data
public class TrajectoryStat {
    @ApiModelProperty("车牌号")
    private String carNumber;
    @ApiModelProperty(value = "车牌类型")
    private String carType;

    @ApiModelProperty(value = "轨迹数量")
    private Integer traCount;
    @ApiModelProperty(value = "总出行天数")
    private Integer dayCount;

    @ApiModelProperty(value = "平均出行距离")
    private Double avgDistance;
    @ApiModelProperty(value = "总出行距离")
    private Double totalDistance;
    @ApiModelProperty(value = "平均每天出行距离")
    private Double avgDistancePerDay;

    @ApiModelProperty(value = "总出行时间")
    private Double totalTime;
    @ApiModelProperty(value = "平均出行时间")
    private Double avgTime;
    @ApiModelProperty(value = "平均每天出行时间")
    private Double avgTimePerDay;


    @ApiModelProperty(value = "平均速度 ")
    private Double avgSpeed;
}
