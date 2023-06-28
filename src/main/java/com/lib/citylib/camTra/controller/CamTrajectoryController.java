package com.lib.citylib.camTra.controller;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.Query.QueryVehicleAppearanceByCar;
import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.dto.*;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;



import com.lib.citylib.camTra.utils.CommonResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    @PostMapping("/listByCarNumberAndCamIdsOrderInTimeRange")
    public CommonResult listByCarNumberAndCamIdsOrderInTimeRange(@RequestBody TrajectoryDtoByCamsAndTimeRange trajectoryDtoByCamsAndTimeRange) throws Exception {
        List<CarTrajectory> carTrajectories = camTrajectoryService.listByCarNumberAndCamIdOrderInTimeRange(
                trajectoryDtoByCamsAndTimeRange.getCarNumber(),
                trajectoryDtoByCamsAndTimeRange.getStartTime(),
                trajectoryDtoByCamsAndTimeRange.getEndTime(),
                trajectoryDtoByCamsAndTimeRange.getCamIds());
        ObjectNode geoJSON = convertToGeoJSON(carTrajectories);
        return CommonResult.success(geoJSON);
    }

    @ResponseBody
    @PostMapping("/listByCarNumberOrderInTimeRange")
    public CommonResult listByCarNumberOrderInTimeRange(@RequestBody VehicleAppearanceByCarDto vehicleAppearanceByCarDto) throws Exception {
        List<CarTrajectory> carTrajectories = camTrajectoryService.listByCarNumberOrderInTimeRange(
                vehicleAppearanceByCarDto.getCarNumbers(),
                vehicleAppearanceByCarDto.getStartTime(),
                vehicleAppearanceByCarDto.getEndTime());
        ObjectNode geoJSON = convertToGeoJSON(carTrajectories);
        return CommonResult.success(geoJSON);
    }

    @ResponseBody
    @PostMapping("/searchCarTrajectory")
    public CommonResult searchCarTrajectory(@RequestBody TrajectoryDto trajectoryDto) throws Exception {
        System.out.println(trajectoryDto);
        List<CarTrajectory> carTrajectories = camTrajectoryService.listByTrajectoryDto(trajectoryDto);
        if (carTrajectories.isEmpty())
            return CommonResult.error();
        ObjectNode geoJSON = convertToGeoJSON(carTrajectories);

        return CommonResult.success(geoJSON);
    }

    @ResponseBody
    @PostMapping("/vehicleCountByCam")
    public CommonResult vehicleCountByCam(@RequestBody VehicleCountByCamDto vehicleCountByCamDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<QueryVehicleCountByCam> vehicleCountByCams = camTrajectoryService.vehicleCountByCam(vehicleCountByCamDto);
        if (vehicleCountByCams.isEmpty())
            return CommonResult.error();
        return CommonResult.success(vehicleCountByCams);
    }

    @ResponseBody
    @PostMapping("/vehicleAppearanceByCar")
    public CommonResult vehicleAppearanceByCar(@RequestBody VehicleAppearanceByCarDto vehicleAppearanceByCarDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<QueryVehicleAppearanceByCar> vehicleAppearanceByCars = camTrajectoryService.vehicleAppearanceByCar(vehicleAppearanceByCarDto);
        if (vehicleAppearanceByCars.isEmpty())
            return CommonResult.error();
        return CommonResult.success(vehicleAppearanceByCars);
    }

    @ResponseBody
    @PostMapping("/camCountByCar")
    public CommonResult camCountByCar(@RequestBody CamCountByCarDto camCountByCarDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        if(camCountByCarDto.getCarNumber().isEmpty()||camCountByCarDto.getCarNumber().equals("")) {
            return CommonResult.error("未返回车牌号");
        }
        List<QueryCamCountByCar> queryCamCountByCars = camTrajectoryService.listCamCountByCar(camCountByCarDto);
        if(queryCamCountByCars.size() ==0){
            return CommonResult.error("暂无数据");
        }
        return CommonResult.success(queryCamCountByCars);
    }

    @GetMapping("/insert")
    public void insert() throws IOException {
        camTrajectoryService.insert();
    }

    public ObjectNode convertToGeoJSONFeature(CarTrajectory carTrajectory) {
        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建特征对象
        ObjectNode feature = JsonNodeFactory.instance.objectNode();
        feature.put("type", "Feature");

        // 创建属性节点
        ObjectNode properties = feature.putObject("properties");
        properties.put("carNumber", carTrajectory.getCarNumber());
        properties.put("carType", carTrajectory.getCarType());
        properties.put("distance", carTrajectory.getDistance());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        properties.put("startTime", dateFormat.format(carTrajectory.getStartTime()));
        properties.put("endTime", dateFormat.format(carTrajectory.getEndTime()));
        properties.put("timeInterval", carTrajectory.getTimeInterval());

        // 创建几何节点
        ObjectNode geometry = feature.putObject("geometry");
        geometry.put("type", "LineString");

        // 创建坐标数组节点
        ArrayNode coordinates = geometry.putArray("coordinates");

        // 添加坐标点
        for (CamTrajectory point : carTrajectory.getPoints()) {
            ArrayNode coordinate = JsonNodeFactory.instance.arrayNode();
            coordinate.add(point.getCamLon());
            coordinate.add(point.getCamLat());
            coordinates.add(coordinate);
        }

        return feature;
    }


    public ObjectNode convertToGeoJSON(List<CarTrajectory> carTrajectories) {
        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建根节点
        ObjectNode root = JsonNodeFactory.instance.objectNode();

        // 设置类型为 "FeatureCollection"
        root.put("type", "FeatureCollection");

        // 创建 features 数组节点
        ArrayNode features = root.putArray("features");

        // 添加每个 CarTrajectory 的特征对象到 features 数组中
        for (CarTrajectory carTrajectory : carTrajectories) {
            ObjectNode feature = convertToGeoJSONFeature(carTrajectory);
            features.add(feature);
        }

        return root;
    }

}
