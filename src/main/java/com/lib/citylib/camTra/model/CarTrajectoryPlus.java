package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CarTrajectoryPlus {
    private String carNumber;
    private String carType;
    private List<CamTrajectory> points;
    private Double distance;  // 米
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;
    private Long timeInterval;  // 秒
    private Double avgSpeed;  // km/h
    private Integer pointNum;
    private String tableName;
    private String startCamId;
    private String endCamId;

    public void addPoint(CamTrajectory camTrajectory) {
        points.add(camTrajectory);
    }
    public CarTrajectoryPlus mergePoint(CarTrajectoryPlus carTrajectory) {
        this.points.add(carTrajectory.points.get(0));
        return this;
    }

    public CarTrajectoryPlus(String carNumber, String carType, List<CamTrajectory> points, Double distance, Date startTime, Date endTime, Long timeInterval, Double avgSpeed, Integer pointNum, String tableName, String startCamId, String endCamId) {
        this.carNumber = carNumber;
        this.carType = carType;
        this.points = points;
        this.distance = distance;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeInterval = timeInterval;
        this.avgSpeed = avgSpeed;
        this.pointNum = pointNum;
        this.tableName = tableName;
        this.startCamId = startCamId;
        this.endCamId = endCamId;
    }

    public CarTrajectoryPlus() {
    }

    @Override
    public String toString() {
        String row = carNumber + "-" + carType + "#";
        for (CamTrajectory point : points) {
            String pointTime = String.valueOf(point.getPhotoTime().getTime() / 1000);
            String pointLon = String.valueOf(point.getCamLon());
            String pointLat = String.valueOf(point.getCamLat());
            row += pointTime + "|" + pointLon + "|" + pointLat + "_";
        }
        row = row.substring(0,row.length()-1);
        return row;
    }
}
