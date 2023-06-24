package com.lib.citylib.camTra.controller;

import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.dto.TrajectoryDto;
import com.lib.citylib.camTra.dto.TrajectoryDtoByTimeRange;
import com.lib.citylib.camTra.dto.VehicleCountByCamDto;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.service.CamTrajectoryService;


import com.lib.citylib.camTra.utils.CommonResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/camTra")
public class CamTrajectoryController {
    @Resource
    private CamTrajectoryService camTrajectoryService;


    @ApiOperation(value = "查询车辆轨迹",notes = "根据车牌号查询车辆轨迹")
    @GetMapping("/listByCarNum")
    public List<CamTrajectory> camTraList(String carNumber){
        return camTrajectoryService.listByCarNumber(carNumber);
    }

    @ApiOperation(value = "查询车辆轨迹1",notes = "根据车牌号查询车辆轨迹")
    @GetMapping("/listByCarNum1")
    public CarTrajectory carTraList1(String carNumber){
        return new CarTrajectory(carNumber, camTrajectoryService.listByCarNumber(carNumber));
    }

    @ResponseBody
    @PostMapping("/listByCarNumInTimeRange")
    public CommonResult listByCarNumInTimeRange(@RequestBody TrajectoryDtoByTimeRange trajectoryDtoByTimeRange) throws Exception {
        List<CarTrajectory> carTrajectories = camTrajectoryService.listByCarNumberOrderInTimeRange(
                trajectoryDtoByTimeRange.getCarNumber(),
                trajectoryDtoByTimeRange.getStartTime(),
                trajectoryDtoByTimeRange.getEndTime(),
                trajectoryDtoByTimeRange.getCamIds());

        return CommonResult.success(carTrajectories);
    }

    @ResponseBody
    @PostMapping("/searchCarTrajectory")
    public CommonResult camTraList2(@RequestBody TrajectoryDto trajectoryDto) throws Exception {
        System.out.println(trajectoryDto);
        List<CarTrajectory> carTrajectories = camTrajectoryService.listByTrajectoryDto(trajectoryDto);
        if (carTrajectories.isEmpty())
            return CommonResult.error();
        return CommonResult.success(carTrajectories);
    }

    @ResponseBody
    @PostMapping("/vehicleCountByCam")
    public CommonResult vehicleCountByCam(@RequestBody VehicleCountByCamDto vehicleCountByCamDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<QueryVehicleCountByCam> vehicleCountByCams = camTrajectoryService.vehicleCountByCam(vehicleCountByCamDto);
        if (vehicleCountByCams.isEmpty())
            return CommonResult.error();
        System.out.println(vehicleCountByCams.size());
        return CommonResult.success(vehicleCountByCams);
    }

    @GetMapping("/insert")
    public void insert() throws IOException {
        camTrajectoryService.insert();
    }
}
