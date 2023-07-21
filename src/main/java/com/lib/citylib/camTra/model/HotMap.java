package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
//count最大值默认为100
public class HotMap {
    private Double lng;
    private Double lat;
    private Integer count;

    public HotMap(Double lng, Double lat, Integer count) {
        this.lng = lng;
        this.lat = lat;
        this.count = count;
    }

    public HotMap(Point point, Integer count){
        this.lng = point.getLon();
        this.lat = point.getLat();
        this.count = count;
    }
}
