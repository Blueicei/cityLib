package com.lib.citylib.camTra.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.model.CamInfo;
import lombok.Data;

import java.util.Date;

@Data
public class QueryCamCountByCar {
    /**
     * 卡口号
     */
    private String camId;

    /**
     * 出现次数
     */
    private Long count;

    /**
     *最后的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date lastTime;

    private CamInfo camInfo;


}
