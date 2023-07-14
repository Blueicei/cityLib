package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
public class GirdFlow {
    private String girdId;

    private int count;

    private double centralLon;

    private double centralLat;

    private String poi;

    private int row;

    private int col;

}
