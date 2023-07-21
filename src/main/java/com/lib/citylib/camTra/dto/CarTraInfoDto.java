package com.lib.citylib.camTra.dto;

import com.lib.citylib.camTra.model.CarTrajectory;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class CarTraInfoDto {

    // 单车辆统计信息，轨迹数大于一定阈值（一般是10）
    private String carNumber;
    private String carType;
    private Long traCount; // 轨迹数量
    private Double totalTime; // 总出行时间h，保留2位小数
    private Double totalDistance; // 总出行距离km，保留2位小数
    private Long dayCount; // 总出行天数day
    private Double avgTime; // 平均出行时间h，保留2位小数
    private Double avgDistance; // 平均出行距离km，保留2位小数
    private Double avgSpeed; // 平均出行速度km/h，保留2位小数
    private Double avgTraCountPerDay; // 平均一天的轨迹数，保留2位小数
    private Double avgTimePerDay; // 平均一天的出行时间h，保留2位小数
    private Double avgDistancePerDay; // 平均一天的出行距离km，保留2位小数

    private Map<String, Long> traTimeDistribute; // 行程时间分布 (0,30min],(30min, 1h],(1h,2h],(2h,...)
    private Map<String, Long> traDistanceDistribute; // 行程距离分布 (0, 1km],(1km, 2km],(2km, 3km],...,(10,20km],(20km,30km],(30km,...)
    private Map<String, Long> traStartTimePerHour; // 行程出发时间分布 [0,1,2,3,...,23]
    private Map<String, Long> traStartTimePerDay; // 行程出发时间分布 [1,2,3,...,31]

    private List<String> dayRecord; // yyyy-MM-dd 判断总天数

    public CarTraInfoDto() {
    }

    public CarTraInfoDto updateDto(CarTrajectory carTrajectory){
        this.setTraCount(this.traCount + 1L);
        this.setTotalTime(this.totalTime + (double)carTrajectory.getTimeInterval() / 3600L);
        this.setTotalDistance(this.totalDistance + carTrajectory.getDistance() / 1000L);

        Date startTime = carTrajectory.getPoints().get(0).getPhotoTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String traDate = sdf.format(startTime).toString();
        if(!this.dayRecord.contains(traDate))
        {
            this.dayRecord.add(traDate);
            this.setDayCount(this.dayCount + 1L);
        }

        this.setAvgTime(this.totalTime / this.traCount);
        this.setAvgDistance(this.totalDistance / this.traCount);
        this.setAvgSpeed(this.totalDistance / this.totalTime);
        this.setAvgTraCountPerDay((double)this.traCount / this.dayCount);
        this.setAvgTimePerDay(this.totalTime / this.dayCount);
        this.setAvgDistancePerDay(this.totalDistance /this.dayCount);

        this.updateHashMap(carTrajectory);
        return this;
    }

    public CarTraInfoDto(CarTrajectory carTrajectory){
        String tempCarNumber = carTrajectory.getCarNumber();
        String tempCarType = carTrajectory.getCarType();
        this.setCarNumber(tempCarNumber);
        this.setCarType(tempCarType);
        this.setTraCount(1L);
        this.setTotalTime((double)carTrajectory.getTimeInterval() / 3600L);
        this.setTotalDistance(carTrajectory.getDistance() / 1000L);

        Date startTime = carTrajectory.getPoints().get(0).getPhotoTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String traDate = sdf.format(startTime).toString();
        List<String> traDateList = new ArrayList<>();
        traDateList.add(traDate);
        this.setDayRecord(traDateList);
        this.setDayCount(1L);

        this.setAvgTime(this.totalTime / this.traCount);
        this.setAvgDistance(this.totalDistance / this.traCount);
        this.setAvgSpeed(this.totalDistance / this.totalTime);
        this.setAvgTraCountPerDay((double)this.traCount / this.dayCount);
        this.setAvgTimePerDay(this.totalTime / this.dayCount);
        this.setAvgDistancePerDay(this.totalDistance /this.dayCount);

        this.traTimeDistribute = new HashMap<>();
        this.traTimeDistribute.put("0-30min", 0L);this.traTimeDistribute.put("30min-1h", 0L);this.traTimeDistribute.put("1h-2h", 0L);this.traTimeDistribute.put("2h<", 0L);
        this.traDistanceDistribute = new HashMap<>();
        for(int i=0; i<10;i++){
            String tempKey1 = i + "-" + (i+1) + "km";
            String tempKey2 = (i+1) + "0-" + (i+2) + "0km";
            this.traDistanceDistribute.put(tempKey1, 0L);
            if(i<9){
                this.traDistanceDistribute.put(tempKey2, 0L);
            }
        }
        this.traDistanceDistribute.put("100km<", 0L);
        this.traStartTimePerHour = new HashMap<>();
        for(int i=0; i<24;i++){
            String tempKey = String.valueOf(i);
            if (i<10){
                tempKey = "0" + String.valueOf(i);
            }
            this.traStartTimePerHour.put(tempKey, 0L);
        }
        this.traStartTimePerDay = new HashMap<>();
        for(int i=1; i<32;i++){
            String tempKey = String.valueOf(i);
            if (i<10){
                tempKey = "0" + String.valueOf(i);
            }
            this.traStartTimePerDay.put(tempKey, 0L);
        }

        this.updateHashMap(carTrajectory);
    }

    private void updateHashMap(CarTrajectory carTrajectory){
        Date startTime = carTrajectory.getPoints().get(0).getPhotoTime();
        Long traTime = carTrajectory.getTimeInterval();
        Double traDistance = carTrajectory.getDistance();

        if(0<traTime && traTime<=1800){
            this.traTimeDistribute.put("0-30min", this.traTimeDistribute.get("0-30min") + 1L);
        } else if (1800<traTime && traTime<=3600) {
            this.traTimeDistribute.put("30min-1h", this.traTimeDistribute.get("30min-1h") + 1L);
        } else if (3600<traTime && traTime<=7200) {
            this.traTimeDistribute.put("1h-2h", this.traTimeDistribute.get("1h-2h") + 1L);
        } else {
            this.traTimeDistribute.put("2h<", this.traTimeDistribute.get("2h<") + 1L);
        }
        for(int i=0; i<10;i++){
            String tempKey1 = i + "-" + (i+1) + "km";
            String tempKey2 = (i+1) + "0-" + (i+2) + "0km";
            if ( (i*1000)<traDistance && traDistance<=((i+1)*1000))
            {
                this.traDistanceDistribute.put(tempKey1, this.traDistanceDistribute.get(tempKey1)+1L);
                break;
            }
            if(i<9 && (((i+1)*10000)<traDistance && traDistance<=((i+2)*10000))){
                this.traDistanceDistribute.put(tempKey2, this.traDistanceDistribute.get(tempKey2)+1L);
                break;
            }
            if(traDistance>100000){
                this.traDistanceDistribute.put("100km<", this.traDistanceDistribute.get("100km<")+1L);
                break;
            }
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String[] strNow1 = sdf1.format(startTime).toString().split("-");
        String[] strNow2 = sdf2.format(startTime).toString().split(":");
        String day = strNow1[2];
        String hour = strNow2[0];
        this.traStartTimePerDay.put(day, this.traStartTimePerDay.get(day) + 1L);
        this.traStartTimePerHour.put(hour, this.traStartTimePerHour.get(hour) + 1L);
    }
}
