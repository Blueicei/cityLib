package com.lib.citylib.camTra.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "列表查询通用参数")
public class Pager {
    @ApiModelProperty(value = "分页大小")
    private Integer pageSize = null;
    @ApiModelProperty(value = "第几页")
    private Integer pageNum = null;
    @ApiModelProperty(value = "排序条件")
    private String orderBy;
    @ApiModelProperty(value = "排序条件")
    private Boolean isDesc = false;
}
