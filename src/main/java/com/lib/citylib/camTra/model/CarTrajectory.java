package com.lib.citylib.camTra.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(value = "CarTrajectory",description = "这个类定义了轨迹的所有属性")
@Data
public class CarTrajectory {
    private String carNumber;
    private String carType;
    private List<CamTrajectory> points;
    private Double distance;  // 米
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;
    private Long timeInterval;  // 秒
    public CarTrajectory(List<CamTrajectory> points) {
        this.points = points;
    }

    public CarTrajectory(CamTrajectory point) {
        List<CamTrajectory> tempPoints = new ArrayList<>();
        tempPoints.add(point);
        this.points = tempPoints;
    }
    public CarTrajectory(String carNumber, CamTrajectory point) {
        List<CamTrajectory> tempPoints = new ArrayList<>();
        tempPoints.add(point);
        this.points = tempPoints;
        this.carNumber = carNumber;
    }
    public CarTrajectory(String carNumber, List<CamTrajectory> points) {
        this.points = points;
        this.carNumber = carNumber;
    }
    public CarTrajectory(String carNumber, String carType, List<CamTrajectory> points) {
        this.points = points;
        this.carNumber = carNumber;
        this.carType = carType;
    }
    public CarTrajectory(String carNumber, String carType, List<CamTrajectory> points, Double distance, Date startTime, Date endTime, Long timeInterval) {
        this.carNumber = carNumber;
        this.carType = carType;
        this.points = points;
        this.distance = distance;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeInterval = timeInterval;
    }
    public void addPoint(CamTrajectory camTrajectory) {
        points.add(camTrajectory);
    }
    public CarTrajectory mergePoint(CarTrajectory carTrajectory) {
        this.points.add(carTrajectory.points.get(0));
        return this;
    }

    public List<CamTrajectory> getPoints() {
        return points;
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
