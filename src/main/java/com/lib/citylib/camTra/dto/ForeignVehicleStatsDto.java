package com.lib.citylib.camTra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;
@ApiModel(value = "VehicleCountByCamDto",description = "这个类定义了外部车辆比较传入参数")
@Data
public class ForeignVehicleStatsDto {
    /**
     * 卡口号列表
     */
    private List<String> camIds;
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
     * 分割粒度
     */
    private int granularity;
}
