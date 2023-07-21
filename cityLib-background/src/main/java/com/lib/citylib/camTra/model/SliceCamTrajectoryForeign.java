package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SliceCamTrajectoryForeign {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;

    private int provincialCount;

    private int nonProvincialCount;

    private List<CamTrajectory> trajectories;

    public SliceCamTrajectoryForeign(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.trajectories = new ArrayList<>();
        this.provincialCount = 0;
        this.nonProvincialCount = 0;
    }
}
