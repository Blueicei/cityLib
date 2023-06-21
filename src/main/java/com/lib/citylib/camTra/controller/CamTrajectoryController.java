package com.lib.citylib.camTra.controller;

import com.lib.citylib.camTra.dto.TrajectoryDto;
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
    public CarTrajectory camTraList1(String carNumber){
        return new CarTrajectory(carNumber, camTrajectoryService.listByCarNumber(carNumber));
    }

    @ResponseBody
    @PostMapping("/searchCarTrajectory")
    public CommonResult camTraList2(@RequestBody TrajectoryDto trajectoryDto) throws Exception {
        System.out.println(trajectoryDto);
        List<CarTrajectory> camTrajectories = camTrajectoryService.listByTrajectoryDto(trajectoryDto);
        if (camTrajectories.isEmpty())
            return CommonResult.error();
        return CommonResult.success(camTrajectories);
    }

    @GetMapping("/insert")
    public void insert() throws IOException {
        camTrajectoryService.insert();
    }
}
