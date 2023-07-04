package com.lib.citylib.camTra.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.dto.CamFlowDto;
import lombok.Data;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
public class QueryCamFLow {
    /**
     * 卡口号
     */
    private String camId;

    /**
     * 流量统计（开始时间-结束时间范围内的流量）
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private List<CamFlowDto> camFlows;


}
