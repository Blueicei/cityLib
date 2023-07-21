package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AllInfo {
    private int allCarCount;

    private int flow;

    private int foreignCarCount;

    private int localCarCount;

    private int allCamCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date highestFlowStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date highestFlowEndTime;

    public AllInfo(int allCarCount, int flow, int foreignCarCount, int localCarCount, int allCamCount, Date highestFlowStartTime, Date highestFlowEndTime) {
        this.allCarCount = allCarCount;
        this.flow = flow;
        this.foreignCarCount = foreignCarCount;
        this.localCarCount = localCarCount;
        this.allCamCount = allCamCount;
        this.highestFlowStartTime = highestFlowStartTime;
        this.highestFlowEndTime = highestFlowEndTime;
    }
}
