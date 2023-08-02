package com.lib.citylib.camTra.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.model.CamInfo;
import lombok.Data;

import java.util.Date;

@Data
public class QueryCamCount {
    /**
     * 卡口号
     */
    private String camId;

    /**
     * 出现次数
     */
    private Long count;

    private CamInfo camInfo;


}
