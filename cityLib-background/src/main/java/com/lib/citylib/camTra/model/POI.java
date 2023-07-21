package com.lib.citylib.camTra.model;

import com.lib.citylib.camTra.utils.GPSUtil;
import lombok.Data;

@Data
public class POI {
    private double lon;

    private double lat;

    //0表示是出发地，1表示是目的地
    private int label;

    public POI(double lon, double lat, int label) {
        double[] points = new double[2];
        points = GPSUtil.gps84_To_Gcj02(lat, lon);
        this.lon = points[1];
        this.lat = points[0];
        this.label = label;
    }

    public POI(Point point,int label) {
        double[] points = new double[2];
        points = GPSUtil.gps84_To_Gcj02(point.getLat(), point.getLon());
        this.lon = points[1];
        this.lat = points[0];
        this.label = label;
    }
}
