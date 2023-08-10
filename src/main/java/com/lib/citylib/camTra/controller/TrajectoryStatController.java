package com.lib.citylib.camTra.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.dto.ClusterFlowDto;
import com.lib.citylib.camTra.dto.StartToEndTime;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.CommonResult;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/stat")
public class TrajectoryStatController {
    @Resource
    private TrajectoryStatService trajectoryStatService;
    @Resource
    private PartitionTraUtil partitionTraUtil;

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

    @ResponseBody
    @PostMapping("/getClusterFlow")
    public CommonResult getClusterFlow(@RequestBody ClusterFlowDto clusterFlowDto) {
        return CommonResult.success(trajectoryStatService.getClusterFlow(clusterFlowDto));
    }

    @ResponseBody
    @PostMapping("/getTableStatByTime")
    public CommonResult getTableStatByTime(@RequestBody StartToEndTime startToEndTime){
        System.out.println(startToEndTime);
        return CommonResult.success(trajectoryStatService.getTableStatByTimePlus(startToEndTime));
    }

    @ResponseBody
    @GetMapping("/initStatTableByName")
    public CommonResult initStatTableByName(String tableName) throws Exception {
        return CommonResult.success(partitionTraUtil.partitionTraUtilPlus(tableName));
    }

    @ResponseBody
    @GetMapping("/initStatTableByCSVFile")
    public CommonResult initStatTableByCSVFile(String tableName) {
        return CommonResult.success(partitionTraUtil.partitionTraUtilPlusByCSVFile(tableName));
    }
}
