package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
public class CarTrajectoryWithTerminal {
    private CarTrajectory carTrajectory;

    private Point start;

    private Point end;

    private String carNumber;

    private String startCamId;

    private String endCamId;

    public CarTrajectoryWithTerminal(CarTrajectory carTrajectory, Point start, Point end,String carNumber) {
        this.carTrajectory = carTrajectory;
        this.start = start;
        this.end = end;
        this.carNumber = carNumber;
        this.startCamId = this.carTrajectory.getPoints().get(0).getCamId();
        this.endCamId = this.carTrajectory.getPoints().get(this.carTrajectory.getPoints().size()-1).getCamId();
    }

    public String getStartCamId() {
        return startCamId;
    }

    public String getEndCamId() {
        return endCamId;
    }

}
