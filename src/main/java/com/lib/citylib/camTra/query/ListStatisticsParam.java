package com.lib.citylib.camTra.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "车辆统计信息列表参数")
public class ListStatisticsParam extends Pager{
    @ApiModelProperty(value = "统计时间最小值")
    private String minTime;
    @ApiModelProperty(value = "统计时间最大值")
    private String maxTime;
    @ApiModelProperty(value = "车辆类型")
    private String carType;
    @ApiModelProperty(value = "车牌号")
    private String carNumber;

    private Integer cut;
}
