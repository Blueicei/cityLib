package com.lib.citylib.camTra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.model.CarTrajectory;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class CarTraInfoDto2 {

    // 单车辆统计信息，轨迹数大于一定阈值（一般是10）
    private String carNumber;
    private String carType;
    private Long traCount; // 轨迹数量
    private Double totalTime; // 总出行时间h，保留2位小数
    private Double totalDistance; // 总出行距离km，保留2位小数
    private Long dayCount; // 总出行天数day
    private Double avgTime; // 平均出行时间h，保留2位小数
    private Double avgDistance; // 平均出行距离km，保留2位小数
    private Double avgTraCountPerDay; // 平均一天的轨迹数，保留2位小数
    private Double avgTimePerDay; // 平均一天的出行时间h，保留2位小数
    private Double avgDistancePerDay; // 平均一天的出行距离km，保留2位小数

    private Map<String, Long> traTimeDistribute; // 行程时间分布 (0,30min],(30min, 1h],(1h,2h],(2h,...)
    private Map<String, Long> traDistanceDistribute; // 行程距离分布 (0, 1km],(1km, 2km],(2km, 3km],...,(10,20km],(20km,30km],(30km,...)
    private Map<String, Long> traStartTimePerHour; // 行程出发时间分布 (0,1],(1,2],...(23,24]
    private Map<String, Long> traStartTimePerDay; // 行程出发时间分布 (0,1],(1,2],...(30,31]

    private List<Long> pointNumber;
    private List<Double> traDistance;
    private List<Long> traTime;
    private List<Double> traSpeed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private List<Date> startTime;


    private Map<Long, Long> dayTraCount;
    // 统计一天内的只保留小时
    private Map<Long, Long> dayStartTimeCount;
    public void updateStartTime(Date tempTime){
        if (this.startTime == null || this.startTime.size() == 0){
           this.startTime = new ArrayList<>();
           this.dayTraCount = new HashMap<>();
           this.dayStartTimeCount = new HashMap<>();
           this.dayCount = 0L;
           this.startTime.add(tempTime);
        }
        else{
            this.startTime.add(tempTime);
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String[] strNow1 = sdf1.format(tempTime).toString().split("-");
        String[] strNow2 = sdf2.format(tempTime).toString().split(":");
        Long day = Long.parseLong(strNow1[2]);
        Long hour = Long.parseLong(strNow2[0]);
        if(this.dayTraCount.containsKey(day)){
            this.dayTraCount.put(day, this.dayTraCount.get(day) + 1);
        }
        else {
            this.dayCount += 1;
            this.dayTraCount.put(day, 1L);
        }
        if(this.dayStartTimeCount.containsKey(hour)){
            this.dayStartTimeCount.put(hour, this.dayStartTimeCount.get(hour) + 1);
        }
        else {
            this.dayStartTimeCount.put(hour, 1L);
        }
    }
    public CarTraInfoDto2(){}
    public CarTraInfoDto2(CarTrajectory carTrajectory){
        String tempCarNumber = carTrajectory.getCarNumber();
        String tempCarType = carTrajectory.getCarType();
        this.setCarNumber(tempCarNumber);
        this.setCarType(tempCarType);
        this.setTraCount(1L);

        List<Long> tempPointNumber = new ArrayList<>();
        tempPointNumber.add((long) carTrajectory.getPoints().size());
        this.setPointNumber(tempPointNumber);

        List<Double> tempTraLength = new ArrayList<>();
        tempTraLength.add(carTrajectory.getDistance());
        this.setTraDistance(tempTraLength);

        List<Long> tempTraTime = new ArrayList<>();
        tempTraTime.add((long) carTrajectory.getTimeInterval());
        this.setTraTime(tempTraTime);

        List<Double> tempSpeed = new ArrayList<>();
        tempSpeed.add(carTrajectory.getAvgSpeed());
        this.setTraSpeed(tempSpeed);

//        List<Date> tempStartTime = new ArrayList<>();
//        tempStartTime.add(carTrajectory.getPoints().get(0).getPhotoTime());
//        this.setStartTime(tempStartTime);
        this.updateStartTime(carTrajectory.getPoints().get(0).getPhotoTime());
    }
    public CarTraInfoDto2 updateDto(CarTrajectory carTrajectory){
        this.setTraCount(this.getTraCount() + 1L);

        List<Long> tempPointNumber = this.getPointNumber();
        tempPointNumber.add((long) carTrajectory.getPoints().size());
        this.setPointNumber(tempPointNumber);

        List<Double> tempTraLength = this.getTraDistance();
        tempTraLength.add(carTrajectory.getDistance());
        this.setTraDistance(tempTraLength);

        List<Long> tempTraTime = this.getTraTime();
        tempTraTime.add((long) carTrajectory.getTimeInterval());
        this.setTraTime(tempTraTime);

        List<Double> tempSpeed = this.getTraSpeed();
        tempSpeed.add(carTrajectory.getAvgSpeed());
        this.setTraSpeed(tempSpeed);

//        List<Date> tempStartTime = this.getStartTime();
//        tempStartTime.add(carTrajectory.getPoints().get(0).getPhotoTime());
//        this.setStartTime(tempStartTime);
        this.updateStartTime(carTrajectory.getPoints().get(0).getPhotoTime());
        return this;
    }
}
