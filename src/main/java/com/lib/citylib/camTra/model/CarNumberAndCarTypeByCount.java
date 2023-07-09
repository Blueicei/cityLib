package com.lib.citylib.camTra.model;

import lombok.Data;

@Data
public class CarNumberAndCarTypeByCount {
    private String carNumber;

    private String carType;

    private int count;

    public CarNumberAndCarTypeByCount(String carNumber, int count) {
        this.carNumber = carNumber;
        this.count = count;
    }

    public CarNumberAndCarTypeByCount(String carNumber, String carType) {
        this.carNumber = carNumber;
        this.carType = carType;
    }

    public CarNumberAndCarTypeByCount(String carNumber) {
        this.carNumber = carNumber;
    }

    public CarNumberAndCarTypeByCount(String carNumber, String carType, int count) {
        this.carNumber = carNumber;
        this.carType = carType;
        this.count = count;
    }
}
