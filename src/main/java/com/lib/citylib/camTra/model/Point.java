package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
public class Point {
    private double lon;

    private double lat;

    public Point(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }
}
