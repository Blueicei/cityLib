package com.lib.citylib.camTra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CompareVehicleStats implements Serializable {
    private String camId;
    private List<SliceCamTrajectoryCompare> sliceCamTrajectories;

    public CompareVehicleStats(String s, List<SliceCamTrajectoryCompare> dividedLists) {
        this.camId =s;
        this.sliceCamTrajectories = dividedLists;
    }
}
