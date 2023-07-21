package com.lib.citylib.camTra.Query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class QueryCamByCar {
    /**
     * 卡口号
     */
    private String camID;

    /**
     *最后的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date Time;
}
