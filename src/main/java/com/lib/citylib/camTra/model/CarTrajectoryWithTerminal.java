package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
public class CarTrajectoryWithTerminal {
    private CarTrajectory carTrajectory;

    private Point start;

    private Point end;

    private String carNumber;

    public CarTrajectoryWithTerminal(CarTrajectory carTrajectory, Point start, Point end,String carNumber) {
        this.carTrajectory = carTrajectory;
        this.start = start;
        this.end = end;
        this.carNumber = carNumber;
    }
}
