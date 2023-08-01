package com.lib.citylib.camTra.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.mapper.TaxiTrajectoryMapper;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.TaxiTrajectoryService;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    @GetMapping("/taxiList")
    public CommonResult taxiList(int pageNum, int pageSize){
        return CommonResult.success(taxiTrajectoryService.taxiList(pageNum, pageSize));
    }
    @GetMapping("/saveGpsData")
    public CommonResult saveGpsData(String carNumber){
        // TODO: 2023/7/27  出租车入库程序前端操作流程待更新
        Set<String> exist = taxiTrajectoryMapper.getCarFromStat();
        Set<String> carNumberSet = new HashSet<>(Arrays.asList(carNumber.split(",")));
        carNumberSet.removeAll(exist);
        partitionTraUtil.saveGpsData(carNumberSet);
        return CommonResult.success(null);
    }
}
