package com.lib.citylib.camTra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class RegionDto {
    private double left;

    private double right;

    private double up;

    private double down;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;

    private int trajectoryCut;

    public RegionDto(double left, double right, double up, double down, Date startTime, Date endTime, int trajectoryCut) {

        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        this.startTime = startTime;
        this.endTime = endTime;
        this.trajectoryCut = trajectoryCut;
    }
}
