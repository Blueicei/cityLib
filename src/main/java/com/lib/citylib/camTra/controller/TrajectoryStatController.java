package com.lib.citylib.camTra.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/stat")
public class TrajectoryStatController {
    @Resource
    private TrajectoryStatService trajectoryStatService;

    @PostMapping ("/info")
    public CommonResult getInfo(@RequestBody ListStatisticsParam param){

        return CommonResult.success(trajectoryStatService.getTotalStat(param));
    }

    @ResponseBody
    @PostMapping("/getStatList")
    public CommonResult getStatList(@RequestBody ListStatisticsParam param) throws Exception {
        IPage<TrajectoryStat> res = trajectoryStatService.listWithPage(param);
        return CommonResult.success(res);
    }
    @ResponseBody
    @PostMapping("/getStatByCar")
    public CommonResult getStatByCar(@RequestBody ListStatisticsParam param) throws Exception {
        return CommonResult.success(trajectoryStatService.getStatByCar(param));
    }

}
