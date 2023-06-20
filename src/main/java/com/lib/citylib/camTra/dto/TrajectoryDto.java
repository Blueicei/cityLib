package com.lib.citylib.camTra.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "TrajectoryDto",description = "这个类定义了轨迹查询传入参数")
@Data
public class TrajectoryDto {
    /**
     * 车牌号列表
     */
    private List<String> carNumbers;

    /**
     * 车牌类型
     */
    private List<String> carTypes;

    /**
     *
     */
    private List<String> camIds;

    /**
     *
     */
    private String trajectorycut;

    /**
     *
     */
    private Date startTime;

    /**
     *
     */
    private Date endTime;
}
