package com.lib.citylib.camTra.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel(value = "TrajectoryDto",description = "这个类定义了轨迹查询传入参数")
@Data
public class TrajectoryDto implements Serializable {
    /**
     * 车牌号列表
     */
    private List<String> carNumbers;

    /**
     * 车牌类型列表
     */
    private List<String> carTypes;

    /**
     * 卡口号列表
     */
    private List<String> camIds;

    /**
     * 分割粒度
     */
    private int trajectoryCut;

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
