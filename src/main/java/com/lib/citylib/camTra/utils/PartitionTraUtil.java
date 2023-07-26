package com.lib.citylib.camTra.utils;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.CharsetUtil;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TaxiTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarInfo;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PartitionTraUtil {
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private TrajectoryStatMapper trajectoryStatMapper;
    @Resource
    private TaxiTrajectoryMapper taxiTrajectoryMapper;
    @Resource
    private CamTrajectoryService camTrajectoryService;

    final private Long trajectoryCut = 30L;
    final private Long traLength = 0L;
    final private Long pointNumber = 0L;
    final private boolean filterTraRange = true;


    public void partitionTraUtil(){
        //获取所有车辆
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberList();

        trajectoryStatMapper.clear();

        int segmentSize = 1000;
        //分批取数据
        List<CarTrajectory> newTraList = new ArrayList<>();
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
                        reduceGroup(new CamTrajectoryService.MergeGroupPoints(trajectoryCut, traLength, pointNumber, false));
                newTraList.addAll(newPoints.collect());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newTraList.size() > 100000){
                trajectoryStatMapper.insertBatch(newTraList);
                newTraList = new ArrayList<>();
            }
        }
    }

    @Data
    public static class TrajectoryIn{
        @Alias("车牌")
        private String carNumber;
        @Alias("上车时间")
        private Date startTime;
        @Alias("下车时间")
        private Date endTime;
        @Alias("载客里程(km)")
        @ApiModelProperty(value = "载客里程(km)")
        private Double distanceCarry;
        @Alias("空驶里程(km)")
        @ApiModelProperty(value = "空驶里程(km)")
        private Double distanceEmpty;

        public TaxiTrajectory convertToTaxiTrajectory(){
            TaxiTrajectory taxiTrajectory = new TaxiTrajectory();
            BeanUtils.copyProperties(this,taxiTrajectory);
            return taxiTrajectory;
        }
    }

    public void saveGpsData(String path){
        path = "C:\\Users\\Zhang\\Desktop\\鲁AD00020";
        File directory = new File(path);
        if (directory.isDirectory()){
            String carNumber = directory.getName();
            //订单文件
            File[] tripFiles = directory.listFiles((dir, name) -> name.matches("trip.*"));
            //轨迹点文件
            File[] posFiles = directory.listFiles((dir, name) -> name.matches(".*PosNew.*"));
            if(tripFiles == null || posFiles == null){
                return;
            }
            CsvReader reader = CsvUtil.getReader();
            //保存轨迹信息数据
            List<TrajectoryIn> trajectoryInList = new ArrayList<>();
            for (File tripFile : tripFiles) {
                trajectoryInList.addAll(reader.read(ResourceUtil.getUtf8Reader(tripFile.getAbsolutePath()), TrajectoryIn.class));
            }
            List<TaxiTrajectory> trajectoryList = trajectoryInList.stream().map(TrajectoryIn::convertToTaxiTrajectory).collect(Collectors.toList());
            //保存轨迹点数据
            List<GpsPoint> pointList = new ArrayList<>();
            for (File posFile : posFiles) {
                pointList.addAll(reader.read(ResourceUtil.getUtf8Reader(posFile.getAbsolutePath()), GpsPoint.class));
            }
            pointList = pointList.stream().filter(e -> e.getLat() > 0 && e.getLng() > 0).collect(Collectors.toList());
            //按时间排序
            pointList.sort((o1, o2) -> DateUtil.compare(o1.getTime(),o2.getTime()));
            //维护车牌号信息, 经纬度的坐标系转换
            pointList.forEach(e ->{
                e.setCarNumber(carNumber);
                double[] latLon = GPSUtil.gps84_To_bd09(e.getLat(),e.getLng());
                e.setLat(latLon[0]);
                e.setLng(latLon[1]);
            });
            for (TaxiTrajectory taxiTrajectory : trajectoryList) {
                //为每个轨迹生成id
                String traId = UUID.randomUUID().toString();
                taxiTrajectory.setTraId(traId);
                //将轨迹id分配给对应的轨迹点。按照轨迹的起始时间分配
                List<GpsPoint> tempPoints = pointList
                        .stream()
                        .filter(
                                e-> DateUtil.compare(e.getTime(),taxiTrajectory.getStartTime()) > 0 && DateUtil.compare(e.getTime(),taxiTrajectory.getEndTime()) <= 0
                        ).collect(Collectors.toList());
                tempPoints.forEach(e -> e.setTraId(traId));
                double distanceCal = 0D;
                for (int i = 1; i < tempPoints.size(); i ++) {
                    distanceCal += camTrajectoryService.GetDistance(
                            tempPoints.get(i - 1).getLng(), tempPoints.get(i - 1).getLat(),
                            tempPoints.get(i).getLng(), tempPoints.get(i).getLat());
                }
                taxiTrajectory.setDistanceCal(distanceCal / 1000);
            }
            CsvWriter writer = CsvUtil.getWriter(path+"\\outTra.csv", CharsetUtil.CHARSET_UTF_8);
            writer.writeBeans(trajectoryList);
            writer.close();

            writer = CsvUtil.getWriter(path+"\\outPoint.csv", CharsetUtil.CHARSET_UTF_8);
            writer.writeBeans(pointList);
            writer.close();
//            taxiTrajectoryMapper.insertBatch(trajectoryList);
//            taxiTrajectoryMapper.insertPoints(pointList);
        }
    }

}
