package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Data
public class SliceCamTrajectoryCompare {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;

    private int count;

    private List<CamTrajectory> trajectories;

    public SliceCamTrajectoryCompare(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.trajectories = new ArrayList<>();
    }
}
