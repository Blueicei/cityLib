package com.lib.citylib.camTra.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.TaxiTrajectoryService;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.CommonResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/taxi")
public class TaxiTrajectoryController {
    @Resource
    private TaxiTrajectoryService taxiTrajectoryService;

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

    @GetMapping("/getPointByTra")
    public CommonResult getPointByTra(String traId) {
        return CommonResult.success(taxiTrajectoryService.getPointByTra(traId));
    }
    @PostMapping("/getPointByCar")
    public CommonResult getPointByCar(@RequestBody ListStatisticsParam param) {
        return CommonResult.success(taxiTrajectoryService.getPointByCar(param));
    }
}
