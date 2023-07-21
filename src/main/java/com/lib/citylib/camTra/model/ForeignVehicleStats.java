package com.lib.citylib.camTra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ForeignVehicleStats implements Serializable{
    private String camId;
    private List<SliceCamTrajectoryForeign> sliceCamTrajectories;

    public ForeignVehicleStats(String s, List<SliceCamTrajectoryForeign> dividedLists) {
        this.camId =s;
        this.sliceCamTrajectories = dividedLists;
    }
}
