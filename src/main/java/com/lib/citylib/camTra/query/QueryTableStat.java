package com.lib.citylib.camTra.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.dto.CamFlowDto;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class QueryTableStat {
    private String tableName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date queryStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date queryEndTime;
    private Long traCount;
    private Long carTypeCount;
    private Long carNumberCount;
    private Long pointNumberCount;
    private Map<String, Long> carTypeTraCountMap;
    private Map<String, Long> dateTraCountMap;
    private Map<Object,Long> traTimeDistribute;
    private Map<Object, Long> traDistanceDistribute;
    private Map<Object, Long> traStartTimePerHour;
    private Map<Object, Long> traStartTimePerDay;
    private Map<Object, Long> traCountByCar;

}
