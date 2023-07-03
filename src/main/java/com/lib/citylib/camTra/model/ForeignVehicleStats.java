package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ForeignVehicleStats implements Serializable{
    private String camId;
    private List<SliceCamTrajectory> sliceCamTrajectories;

    public ForeignVehicleStats(String s, List<SliceCamTrajectory> dividedLists) {
        this.camId =s;
        this.sliceCamTrajectories = dividedLists;
    }
}
