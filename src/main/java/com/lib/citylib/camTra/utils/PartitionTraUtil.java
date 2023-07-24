package com.lib.citylib.camTra.utils;

import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarInfo;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class PartitionTraUtil {
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private TrajectoryStatMapper trajectoryStatMapper;

    final private Long trajectoryCut = 30L;
    final private Long traLength = 0L;
    final private Long pointNumber = 0L;
    final private boolean filterTraRange = true;


    public void partitionTraUtil(){
        //获取所有车辆
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberList();

        trajectoryStatMapper.clear();

        QueryGenerateResult queryGenerateResult = new QueryGenerateResult();
        int segmentSize = 1000;
        //分批取数据
        for (int i = 0; i < carList.size(); i += segmentSize) {
            int endIndex = Math.min(i + segmentSize, carList.size());
            List<CarInfo> segment = carList.subList(i, endIndex);
            List<CamTrajectory> camPointList = camTrajectoryMapper.getPartialCarPointInCondition(segment, filterTraRange);
            try {
                ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                DataSet<CamTrajectory> points = env.fromCollection(camPointList).name("row-camtra-points");
                //划分轨迹
                DataSet<CarTrajectory> newPoints = points.
                        distinct().
                        groupBy(CamTrajectory::getCarNumber).
                        sortGroup(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        reduceGroup(new CamTrajectoryService.MergeGroupPoints(trajectoryCut, traLength, pointNumber)).
                        name("points-to-trajectory");
                List<CarTrajectory> newTraList = newPoints.collect();
                trajectoryStatMapper.insertBatch(newTraList);
            } catch (Exception e) {
                e.printStackTrace();
                queryGenerateResult.setMsg("error");
            }
        }
    }
}
