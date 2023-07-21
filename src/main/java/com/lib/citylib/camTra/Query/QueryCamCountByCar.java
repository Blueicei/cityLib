package com.lib.citylib.camTra.Query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class QueryCamCountByCar {
    /**
     * 卡口号
     */
    private String camID;

    /**
     * 出现次数
     */
    private int count;

    /**
     *最后的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date lastTime;


}
