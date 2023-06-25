package com.lib.citylib.camTra.Query;

import lombok.Data;

@Data
public class QueryVehicleAppearanceByCar {
    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 出现次数
     */
    private int count;

    public QueryVehicleAppearanceByCar(String carNumber, int count) {
        this.carNumber = carNumber;
        this.count = count;
    }
}
