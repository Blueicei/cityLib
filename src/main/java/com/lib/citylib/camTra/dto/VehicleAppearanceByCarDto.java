package com.lib.citylib.camTra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "VehicleAppearanceByCarDto",description = "这个类定义了车辆出现统计传入参数")
@Data
public class VehicleAppearanceByCarDto {
    /**
     * 车牌号列表
     */
    private List<String> carNumbers;
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
}
