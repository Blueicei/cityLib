package com.lib.citylib.camTra.Query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
public class QueryVehicleCountByCam {
    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 出现次数
     */
    private int count;


    /**
     *经过的卡口
     */
    private List<String> Cams;

    public QueryVehicleCountByCam(String carNumber, String camId,int count){
        this.carNumber = carNumber;
        this.Cams = new ArrayList<>();
        this.Cams.add(camId);
        this.count = count;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public void addCam(String camIds) {
        this.Cams.add(camIds);
    }
}
