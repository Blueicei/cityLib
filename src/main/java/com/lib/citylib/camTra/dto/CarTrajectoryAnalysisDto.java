package com.lib.citylib.camTra.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "CarTrajectoryAnalysisDto",description = "这个类定义了车辆轨迹分析传入参数")
public class CarTrajectoryAnalysisDto {
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;

    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 轨迹切分时间
     */
    private int cutTime;
}
