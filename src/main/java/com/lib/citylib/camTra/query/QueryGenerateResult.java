package com.lib.citylib.camTra.query;

import com.lib.citylib.camTra.dto.CarTraInfoDto;
import com.lib.citylib.camTra.dto.CarTraInfoDto2;
import com.lib.citylib.camTra.dto.TableProcessDto;
import com.lib.citylib.camTra.model.CarTrajectory;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class QueryGenerateResult {
    private TableProcessDto tableProcess;
    private String msg;
    private Long carCount;
    private Long pointCount;
    private Long traCount;
    private HashMap<String, CarTraInfoDto> carMap;
    private Long filterTraCount;

    public QueryGenerateResult(){
        this.carCount = 0L;
        this.pointCount = 0L;
        this.traCount = 0L;
        this.traCount = 0L;
        this.filterTraCount = 10L;
        this.carMap = new HashMap<>();
    }
    public void update(List<CarTrajectory> newTraList){
        HashMap<String, Integer> traCountMap = new HashMap<>();
        for (CarTrajectory carTrajectory : newTraList)
        {
            String tempCarNumber = carTrajectory.getCarNumber();
            if (traCountMap.containsKey(tempCarNumber) ){
                traCountMap.put(tempCarNumber, traCountMap.get(tempCarNumber) + 1);
            }
            else{
                carCount += 1;
                traCountMap.put(tempCarNumber, 0);
            }
        }

        for (CarTrajectory carTrajectory : newTraList)
        {
            String tempCarNumber = carTrajectory.getCarNumber();
            String tempCarType = carTrajectory.getCarType();
            traCount += 1;
            pointCount += carTrajectory.getPoints().size();
            if (traCountMap.get(tempCarNumber) >= this.filterTraCount)
            {
                if (this.carMap.containsKey(tempCarNumber) ){
                    CarTraInfoDto tempCarTraInfo =this.carMap.get(tempCarNumber);
                    this.carMap.put(tempCarNumber, tempCarTraInfo.updateDto(carTrajectory));
                }
                else {
                    CarTraInfoDto tempCarTraInfo = new CarTraInfoDto(carTrajectory);
                    this.carMap.put(tempCarNumber, tempCarTraInfo);
                }
            }
        }
    }
}
