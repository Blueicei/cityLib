package com.lib.citylib.camTra.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.dto.StartToEndTime;
import com.lib.citylib.camTra.mapper.TaxiTrajectoryMapper;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.query.QueryODParam;
import com.lib.citylib.camTra.service.TaxiTrajectoryService;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/taxi")
public class TaxiTrajectoryController {
    @Resource
    private TaxiTrajectoryService taxiTrajectoryService;
    @Resource
    private TaxiTrajectoryMapper taxiTrajectoryMapper;
    @Resource
    private PartitionTraUtil partitionTraUtil;

    @PostMapping ("/stat")
    public CommonResult getInfo(@RequestBody ListStatisticsParam param){

        return CommonResult.success(taxiTrajectoryService.getTotalStat(param));
    }
    @ResponseBody
    @PostMapping("/getTableStat")
    public CommonResult getTableStatByTime(@RequestBody ListStatisticsParam param){
        return CommonResult.success(taxiTrajectoryService.getTableStatByTimePlus(param));
    }

    @ResponseBody
    @PostMapping("/getStatList")
    public CommonResult getStatList(@RequestBody ListStatisticsParam param) throws Exception {
        IPage<TrajectoryStat> res = taxiTrajectoryService.listWithPage(param);
        return CommonResult.success(res);
    }
    @ResponseBody
    @PostMapping("/getStatByCar")
    public CommonResult getStatByCar(@RequestBody ListStatisticsParam param) throws Exception {
        return CommonResult.success(taxiTrajectoryService.getStatByCar(param));
    }

    @PostMapping("/getTraByCar")
    public CommonResult getTraByCar(@RequestBody ListStatisticsParam param) throws Exception {
        return CommonResult.success(taxiTrajectoryService.getTraByCar(param));
    }

    @GetMapping("/getGpsPointByTra")
    public CommonResult getPointByTra(String traId) {
        return CommonResult.success(taxiTrajectoryService.getGpsPointByTra(traId));
    }
    @PostMapping("/getGpsPointByCar")
    public CommonResult getPointByCar(@RequestBody ListStatisticsParam param) {
        return CommonResult.success(taxiTrajectoryService.getGpsPointByCar(param));
    }
    @PostMapping("/getCamPointByCar")
    public CommonResult getCamPointByCar(@RequestBody ListStatisticsParam param) {
        return CommonResult.success(taxiTrajectoryService.getCamPointByCar(param));
    }
    @PostMapping("/getGpsPoints")
    public CommonResult getGpsPoints(@RequestBody ListStatisticsParam param) throws Exception {
        return CommonResult.success(taxiTrajectoryService.getGpsPoints(param));
    }
    @Deprecated
    @PostMapping("/getGpsPointsWithCut")
    public CommonResult getGpsPointsWithCut(@RequestBody ListStatisticsParam param) throws Exception {
        return CommonResult.success(taxiTrajectoryService.getGpsPointsWithCut(param));
    }
    @PostMapping("/getODs")
    public CommonResult getODs(@RequestBody ListStatisticsParam param) {
        return CommonResult.success(taxiTrajectoryService.getOds(param));
    }
    @PostMapping("/getODPairs")
    public CommonResult getODPairs(@RequestBody ListStatisticsParam param) {
        return CommonResult.success(taxiTrajectoryService.getOdPairs(param));
    }
    @PostMapping("/getODsByCluster")
    public CommonResult getODs(@RequestBody QueryODParam param) {
        return CommonResult.success(taxiTrajectoryService.getODsByCluster(param));
    }


    @GetMapping("/taxiList")
    public CommonResult taxiList(int pageNum, int pageSize){
        return CommonResult.success(taxiTrajectoryService.taxiList(pageNum, pageSize));
    }
    @GetMapping("/saveGpsData")
    public CommonResult saveGpsData(String carNumber){
        Set<String> exist = taxiTrajectoryMapper.getCarFromStat();
        Set<String> carNumberSet = new HashSet<>(Arrays.asList(carNumber.split(",")));
        carNumberSet.removeAll(exist);
        partitionTraUtil.saveGpsData(carNumberSet);
        return CommonResult.success(null);
    }

    @GetMapping("/saveGps")
    public CommonResult saveGps(int size){
        IPage<HashMap<String,Object>> taxiList = taxiTrajectoryService.taxiList(1, size);
        Set<String> carNumberSet = new HashSet<>();
        for (HashMap<String, Object> record : taxiList.getRecords()) {
            carNumberSet.add((String) record.get("carNumber"));
        }
        Set<String> exist = taxiTrajectoryMapper.getCarFromStat();
        carNumberSet.removeAll(exist);
        partitionTraUtil.saveGpsData(carNumberSet);
        return CommonResult.success(null);
    }
}
