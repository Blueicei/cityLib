package com.lib.citylib.camTra.controller;

import com.alibaba.fastjson.JSONObject;
import com.lib.citylib.camTra.dto.CityFlowDto;
import com.lib.citylib.camTra.dto.TableProcessDto;
import com.lib.citylib.camTra.model.CamInfo;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.TableInfo;
import com.lib.citylib.camTra.query.QueryCamFLow;
import com.lib.citylib.camTra.query.QueryDataSource;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.service.CamTrajectoryService;

import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.DirectoryStructure;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/camTra")
public class CamTrajectoryController {
    @Resource
    private CamTrajectoryService camTrajectoryService;

    @ApiOperation(value = "查询车辆轨迹", notes = "根据车牌号查询车辆轨迹")
    @GetMapping("/listByCarNum")
    public List<CamTrajectory> camTraList(String carNumber) {
        return camTrajectoryService.listByCarNumber(carNumber);
    }

    @ApiOperation(value = "查询车辆轨迹1", notes = "根据车牌号查询车辆轨迹")
    @GetMapping("/listByCarNum1")
    public CarTrajectory camTraList1(String carNumber) {
        return new CarTrajectory(carNumber, camTrajectoryService.listByCarNumber(carNumber));
    }

    @GetMapping("/insert")
    public void insert() throws IOException {
        camTrajectoryService.insert();
    }

    @ResponseBody
    @PostMapping("/countCityFlow")
    public CommonResult countCityFlow(@RequestBody CityFlowDto cityFlowDto) {
//        System.out.println(cityFlowDto);
        List<QueryCamFLow> queryCamFLowList = camTrajectoryService.countCityFlow(cityFlowDto);
        if (queryCamFLowList.isEmpty())
            return CommonResult.error();
        return CommonResult.success(queryCamFLowList);
    }

    @ResponseBody
    @PostMapping("/getAllCamInfo")
    public CommonResult getAllCamInfo() {
        List<CamInfo> camInfoList = camTrajectoryService.getAllCamInfo();
        if (camInfoList.isEmpty())
            return CommonResult.error();

        List<Map<String, Object>> result = new ArrayList<>();
        for (CamInfo camInfo : camInfoList) {
            Map<String, Object> info = new HashMap<>();
//            List<String> lnglat = new ArrayList<>();
//            lnglat.add(String.valueOf(camInfo.getCamLon()));
//            lnglat.add(String.valueOf(camInfo.getCamLat()));
//            info.put("lnglat", lnglat);
            info.put("id", camInfo.getCamId());
            info.put("info", camInfo.getCamAddress());
            info.put("lng", camInfo.getCamLon());
            info.put("lat", camInfo.getCamLat());
            result.add(info);
        }
        return CommonResult.success(result);
    }

    @ResponseBody
    @GetMapping("/getDirectoryStructure")
    public CommonResult getInfo() {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        DirectoryStructure d = new DirectoryStructure();
//        d.scan("/home/fuke/data/origin");
        d.scan("D:\\CodeSearchNet");
//        System.out.println(d.getBuf().toString());
        JSONObject jsonObject = JSONObject.parseObject(d.getBuf().toString());
        return CommonResult.success(jsonObject);
    }

    @ResponseBody
    @PostMapping("/importData")
    public CommonResult importData(@RequestBody List<String> paths) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String tableName = "camtrajectory_" + formatter.format(new Date());
        String path = String.join((","), paths);
//        try {
//            Process proc = Runtime.getRuntime().exec("python3 ./importData.py -f " + path + " -t " + tableName);
//            proc.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return CommonResult.success(tableName);
    }

    @ResponseBody
    @GetMapping("/getTables")
    public CommonResult getTables() {
        List<TableInfo> tables = camTrajectoryService.getTableNameList(null);
        return CommonResult.success(tables);
    }
    @ResponseBody
    @GetMapping("/getCarTypes")
    public CommonResult getCarTypes() {
        List<String> carTypeList = camTrajectoryService.getCarTypeList();
        return CommonResult.success(carTypeList);
    }

    @ResponseBody
    @PostMapping("/generateTra")
    public CommonResult generateTra(@RequestBody TableProcessDto tableProcessDto) {
        QueryGenerateResult queryGenerateResult = camTrajectoryService.generateTra(tableProcessDto);
        return CommonResult.success(queryGenerateResult);
    }
    @ResponseBody
    @PostMapping("/changeDataSource")
    public CommonResult changeDataSource(@RequestBody List<String> tableName){
        QueryDataSource queryDataSource = camTrajectoryService.changeDataSource(tableName.get(0));
        return CommonResult.success(queryDataSource);
    }
}
