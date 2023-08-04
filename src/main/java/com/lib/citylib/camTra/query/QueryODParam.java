package com.lib.citylib.camTra.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "查询指定范围的流向")
public class QueryODParam extends Pager{
    private double minLng;
    private double minLat;
    private double maxLng;
    private double maxLat;
    private String minTime;
    private String maxTime;
}
