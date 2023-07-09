package com.lib.citylib.camTra.controller;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.Query.QueryCityFlowStats;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.Query.QueryVehicleAppearanceByCar;
import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.dto.*;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;



import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.GPSUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/camTra")
public class CamTrajectoryController {
    @Resource
    private CamTrajectoryService camTrajectoryService;

    /**
     * 返回车牌号，车型和出现次数（系统首页用）
     * @return
     */
    @ResponseBody
    @PostMapping("/listAllCarNumberAndCarTypeByCount")
    public CommonResult listAllCarNumberAndCarTypeByCount(){
//        List<CarNumberAndCarTypeByCount> carNumberAndCarTypeByCounts = camTrajectoryService.listAllCarNumberAndCarTypeByCount();

        return CommonResult.success(camTrajectoryService.listAllCarNumberAndCarTypeByCount());
    }

    /**
     * 获取所有基本信息（首页）
     * @return
     */
    @ResponseBody
    @PostMapping("/listAllInfo")
    public CommonResult listAllInfo() throws ParseException {
        int allCarCount = camTrajectoryService.allCarCount();
        int allCamCount = camTrajectoryService.allCamCount();
        int localCarCount = camTrajectoryService.localCarCount();
        int flow = camTrajectoryService.flow();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = format.parse("2021-02-01 00:00:00");
        Date endTime = format.parse("2021-02-01 23:59:59");
        Map<String, Integer> camTrajectories = camTrajectoryService.highestFlowTime(startTime,endTime);
        String targethour = String.valueOf(camTrajectories.get("hour"));
        System.out.println(targethour);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        Date start = new Date();
        Date end = new Date();
//        String start = "";
//        String end = "";
        while (calendar.getTime().before(endTime) || calendar.getTime().equals(endTime)) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if (hour == Integer.parseInt(targethour)) {
                // 找到hour等于17的时间段
                Date hourStartTime = calendar.getTime();

                // 增加1小时
                calendar.add(Calendar.HOUR_OF_DAY, 1);

                // 获取hour17结束时间
                Date hourEndTime = calendar.getTime();


                start = hourStartTime;
                end = hourEndTime;

                break;
            }

            // 增加1小时
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        AllInfo allInfo = new AllInfo(allCarCount,flow,flow-localCarCount,localCarCount,allCamCount,start,end);
        return CommonResult.success(allInfo);
    }

    /**
     * 返回所有车牌号
     * @return
     */
    @ResponseBody
    @PostMapping("/getAllCarNumber")
    public CommonResult getAllCarNumber(){
        return CommonResult.success(camTrajectoryService.getAllCarNumber());
    }

    /**
     * 分时间段的流量汇总信息
     * @return
     */
    @ResponseBody
    @PostMapping("/getAllFlowStats")
    public CommonResult getAllFlowStats(){
        return CommonResult.success(camTrajectoryService.getAllFlowStats());
    }

    /**
     * 根据时间段返回热力图信息
     * @param startToEndTime
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/getHotMapInfoByTime")
    public CommonResult getHotMapInfoByTime(@RequestBody StartToEndTime startToEndTime) throws Exception{
        List<HotMap> list = camTrajectoryService.getHotMapInfoByTime(startToEndTime);
        if(list.isEmpty()){
            return CommonResult.error("暂无数据");
        }
        return CommonResult.success(list);
    }
    
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

    /**
     * 获取所有卡口信息
     * @return
     */
    @ResponseBody
    @PostMapping("/getAllCamInfo")
    public CommonResult getAllCamInfo(){
        List<CamInfo> camInfoList = camTrajectoryService.getAllCamInfo();
        if (camInfoList.isEmpty())
            return CommonResult.error();

        List<Map<String, Object>> result = new ArrayList<>();
        for (CamInfo camInfo : camInfoList) {
            Map<String, Object> info = new HashMap<>();
            List<String> lnglat = new ArrayList<>();
            double[] doubles = GPSUtil.gps84_To_Gcj02(camInfo.getCamLat(),camInfo.getCamLon());
            lnglat.add(String.valueOf(doubles[1]));
            lnglat.add(String.valueOf(doubles[0]));
            info.put("lnglat", lnglat);
            info.put("camId", camInfo.getCamId());
            info.put("camAddress", camInfo.getCamAddress());
            result.add(info);
        }

        return CommonResult.success(result);
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

    /**
     * 搜索车辆轨迹
     * @param trajectoryDto
     * @return
     * @throws Exception
     */
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
    @PostMapping("/vehicleCamStats")
    public CommonResult vehicleCamStats(@RequestBody VehicleCountByCamDto vehicleCountByCamDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<QueryVehicleCountByCam> vehicleCountByCams = camTrajectoryService.vehicleCountByCam(vehicleCountByCamDto);
        if (vehicleCountByCams.isEmpty())
            return CommonResult.error();
        return CommonResult.success(vehicleCountByCams);
    }

    @ResponseBody
    @PostMapping("/cityFlowStats")
    public CommonResult cityFlowStats(@RequestBody VehicleCountByCamDto vehicleCountByCamDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<QueryCityFlowStats> cityFlowStats = camTrajectoryService.cityFlowStats(vehicleCountByCamDto);
        if (cityFlowStats.isEmpty())
            return CommonResult.error();
        return CommonResult.success(cityFlowStats);
    }

    @ResponseBody
    @PostMapping("/foreignVehiclesStats")
    public CommonResult foreignVehiclesStats(@RequestBody ForeignVehicleStatsDto foreignVehicleStatsDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<ForeignVehicleStats> foreignVehicleStats = camTrajectoryService.foreignVehiclesStats(foreignVehicleStatsDto);
        if (foreignVehicleStats.isEmpty())
            return CommonResult.error();
        return CommonResult.success(foreignVehicleStats);
    }

    /**
     * 未改
     * @param regionDto
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/multiRegionAnalysis")
    public CommonResult multiRegionAnalysis(@RequestBody RegionDto regionDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<CarTrajectory> carTrajectories = camTrajectoryService.multiRegionAnalysis(regionDto);
        if (carTrajectories.isEmpty())
            return CommonResult.error();
        ObjectNode geoJSON = convertToGeoJSON(carTrajectories);
        return CommonResult.success(geoJSON);
    }

    @ResponseBody
    @PostMapping("/regionDestinationAnalysis")
    public CommonResult regionDestinationAnalysis(@RequestBody RegionDto regionDto) throws Exception {
//        System.out.println(vehicleCountByCamDto);
        List<CarTrajectoryWithTerminal> carTrajectoryWithTerminals = camTrajectoryService.regionDestinationAnalysis(regionDto);
        if (carTrajectoryWithTerminals.isEmpty())
            return CommonResult.error();
        return CommonResult.success(carTrajectoryWithTerminals);
    }

    @ResponseBody
    @PostMapping("/vehicleAppearanceByCarStats")
    public CommonResult vehicleAppearanceByCarStats(@RequestBody VehicleAppearanceByCarDto vehicleAppearanceByCarDto) throws Exception {
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
        if(camCountByCarDto.getCarNumber().isEmpty()){
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

    @ResponseBody
    @PostMapping("/carTrajectoryAnalysis")
    public CommonResult carTrajectoryAnalysis(@RequestBody CarTrajectoryAnalysisDto carTrajectoryAnalysisDto) throws Exception{
        List<CarTrajectoryWithTerminal> list = camTrajectoryService.carTrajectoryAnalysis(carTrajectoryAnalysisDto);
        if(list.isEmpty()){
            return CommonResult.error("暂无数据");
        }
        return CommonResult.success(list);
    }

    @ResponseBody
    @PostMapping("/getHotMap")
    public CommonResult getHotMap(@RequestBody CarTrajectory carTrajectory) throws Exception{
        List<HotMap> list = camTrajectoryService.getHotMap(carTrajectory);
        if(list.isEmpty()){
            return CommonResult.error("暂无数据");
        }
        return CommonResult.success(list);
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
            double[] doubles = GPSUtil.gps84_To_Gcj02(point.getCamLat(),point.getCamLon());
            coordinate.add(doubles[1]);
            coordinate.add(doubles[0]);
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
