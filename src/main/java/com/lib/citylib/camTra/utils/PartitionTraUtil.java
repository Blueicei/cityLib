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
import com.lib.citylib.camTra.model.CarTrajectoryPlus;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static com.lib.citylib.camTra.service.CamTrajectoryService.FileWriteList;

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
    @Resource
    private ReplaceTableInterceptor replaceTableInterceptor;
    private static final Logger logger = LoggerFactory.getLogger(PartitionTraUtil.class);

    final private Long trajectoryCut = 30L;
    final private Long traLength = 0L;
    final private Long pointNumber = 0L;
    final private boolean filterTraRange = true;

    public String partitionTraUtilPlusByCSVFile(String tableName){
//        trajectoryStatMapper.deleteTable(tableName);
        String tempTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        //获取所有车辆
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberList();

        int segmentSize = 1000;
        //分批取数据
        List<CarTrajectoryPlus> newTraList = new ArrayList<>();
        for (int i = 0; i < carList.size(); i += segmentSize) {
            int endIndex = Math.min(i + segmentSize, carList.size());
            List<CarInfo> segment = carList.subList(i, endIndex);
            List<CamTrajectory> camPointList = camTrajectoryMapper.getPartialCarPointInCondition(segment, filterTraRange);
            try {
                ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                DataSet<CamTrajectory> points = env.fromCollection(camPointList).name("row-camtra-points");
                //划分轨迹
                DataSet<CarTrajectoryPlus> newPoints = points.
                        distinct().
                        groupBy(CamTrajectory::getCarNumber).
                        sortGroup(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        reduceGroup(new CamTrajectoryService.MergeGroupPointsPlus(trajectoryCut, traLength, pointNumber, false,tableName));
                newTraList.addAll(newPoints.collect());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newTraList.size() > 100000){
//                trajectoryStatMapper.insertBatchPlus(newTraList);
                FileWriteList("./out/tra_stat_" + tableName + ".csv", newTraList, true);
                newTraList = new ArrayList<>();
//                break;
            }
        }
        FileWriteList("./out/tra_stat_" + tableName + ".csv", newTraList, true);
        replaceTableInterceptor.setTableName(tempTableName);
        return tableName;
    }
    public String partitionTraUtilPlus(String tableName){
        trajectoryStatMapper.deleteTable(tableName);
        String tempTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        //获取所有车辆
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberList();

        int segmentSize = 1000;
        //分批取数据
        List<CarTrajectoryPlus> newTraList = new ArrayList<>();
        for (int i = 0; i < carList.size(); i += segmentSize) {
            int endIndex = Math.min(i + segmentSize, carList.size());
            List<CarInfo> segment = carList.subList(i, endIndex);
            List<CamTrajectory> camPointList = camTrajectoryMapper.getPartialCarPointInCondition(segment, filterTraRange);
            try {
                ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                DataSet<CamTrajectory> points = env.fromCollection(camPointList).name("row-camtra-points");
                //划分轨迹
                DataSet<CarTrajectoryPlus> newPoints = points.
                        distinct().
                        groupBy(CamTrajectory::getCarNumber).
                        sortGroup(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        reduceGroup(new CamTrajectoryService.MergeGroupPointsPlus(trajectoryCut, traLength, pointNumber, false,tableName));
                newTraList.addAll(newPoints.collect());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (newTraList.size() > 10000){
                trajectoryStatMapper.insertBatchPlus(newTraList);
                newTraList = new ArrayList<>();
            }
        }
        replaceTableInterceptor.setTableName(tempTableName);
        return tableName;
    }


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
            if (newTraList.size() > 10000){
                trajectoryStatMapper.insertBatch(newTraList);
                newTraList = new ArrayList<>();
            }
        }
    }

    @Data
    public static class TrajectoryIn{
        @Alias("tra_id")
        private String traId;
        @Alias("车牌")
        private String carNumber;
        @Alias("上车时间")
        private Date startTime;
        @Alias("下车时间")
        private Date endTime;
        @Alias("载客里程(km)")
        private Double distanceCarry;
        @Alias("空驶里程(km)")
        private Double distanceEmpty;
        @Alias("distance_cal")
        private Double distanceCal;
        @Alias("start_lng")
        private Double startLng;
        @Alias("start_lat")
        private Double startLat;
        @Alias("end_lng")
        private Double endLng;
        @Alias("end_lat")
        private Double endLat;

        public TrajectoryOut convertToTaxiTrajectoryOut(){
            TrajectoryOut trajectoryOut = new TrajectoryOut();
            BeanUtils.copyProperties(this,trajectoryOut);
            trajectoryOut.setStartTime(DateUtil.format(this.getStartTime(),"yyyy-MM-dd HH:mm:ss"));
            trajectoryOut.setEndTime(DateUtil.format(this.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
            return trajectoryOut;
        }
    }
    @Data
    public static class TrajectoryOut{
        @Alias("tra_id")
        private String traId;
        @Alias("car_number")
        private String carNumber;
        @Alias("start_time")
        private String startTime;
        @Alias("end_time")
        private String endTime;
        @Alias("distance_carry")
        private Double distanceCarry;
        @Alias("distance_empty")
        private Double distanceEmpty;
        @Alias("distance_cal")
        private Double distanceCal;
        @Alias("start_lng")
        private Double startLng;
        @Alias("start_lat")
        private Double startLat;
        @Alias("end_lng")
        private Double endLng;
        @Alias("end_lat")
        private Double endLat;
    }
    @Data
    public class GpsPointIn {
        @Alias("car_number")
        private String carNumber;
        private Double lng;
        private Double lat;
        private Date time;
        @Alias("tra_id")
        private String traId;
        public GpsPointOut convertToGpsPointOut(){
            GpsPointOut pointOut = new GpsPointOut();
            BeanUtils.copyProperties(this,pointOut);
            pointOut.setTime(DateUtil.format(this.getTime(),"yyyy-MM-dd HH:mm:ss"));
            return pointOut;
        }
    }
    @Data
    public class GpsPointOut {
        @Alias("car_number")
        private String carNumber;
        private Double lng;
        private Double lat;
        private String time;
        @Alias("tra_id")
        private String traId;
    }

    @Value("${taxi.gps.folder}")
    private String gpsFolder;
    @Value("${taxi.gps.processed.tra}")
    private String traFolder;
    @Value("${taxi.gps.processed.point}")
    private String pointFolder;

    public void saveGpsData(Set<String> carNumberList){
        File folder = new File(gpsFolder);
//        File folder = new File("C:\\Users\\Zhang\\Desktop\\111");
        File[] carDirs = folder.listFiles();
        if (carDirs == null){
            logger.error(folder + ", 文件夹为空");
            return;
        }
        for (File carDir : carDirs) {
            String carNumber = carDir.getName();
            if (!carNumberList.contains(carNumber))
                continue;

            logger.info("正在处理" + carNumber);
            //订单文件
            File[] tripFiles = carDir.listFiles((dir, name) -> name.matches("trip.*"));
            //轨迹点文件
            File[] posFiles = carDir.listFiles((dir, name) -> name.matches(".*PosNew.*"));
            if(tripFiles == null || posFiles == null){
                return;
            }
            CsvReader reader = CsvUtil.getReader();
            //保存轨迹信息数据
            List<TrajectoryIn> trajectoryInList = new ArrayList<>();
            for (File tripFile : tripFiles) {
                trajectoryInList.addAll(reader.read(ResourceUtil.getUtf8Reader(tripFile.getAbsolutePath()), TrajectoryIn.class));
            }
            //保存轨迹点数据
            List<GpsPointIn> pointInList = new ArrayList<>();
            for (File posFile : posFiles) {
                pointInList.addAll(reader.read(ResourceUtil.getUtf8Reader(posFile.getAbsolutePath()), GpsPointIn.class));
            }
            pointInList = pointInList.stream().filter(e -> e.getLat() > 0 && e.getLng() > 0).collect(Collectors.toList());
            //按时间排序
            pointInList.sort((o1, o2) -> DateUtil.compare(o1.getTime(),o2.getTime()));
            //维护车牌号信息, 经纬度的坐标系转换
            pointInList.forEach(e ->{
                e.setCarNumber(carNumber);
                double[] latLon = GPSUtil.gps84_To_bd09(e.getLat(),e.getLng());
                e.setLat(latLon[0]);
                e.setLng(latLon[1]);
            });
            for (TrajectoryIn taxiTrajectory : trajectoryInList) {
                //为每个轨迹生成id
                String traId = UUID.randomUUID().toString();
                taxiTrajectory.setTraId(traId);
                //将轨迹id分配给对应的轨迹点。按照轨迹的起始时间分配
                List<GpsPointIn> tempPoints = pointInList
                        .stream()
                        .filter(
                                e-> DateUtil.compare(e.getTime(),taxiTrajectory.getStartTime()) > 0 && DateUtil.compare(e.getTime(),taxiTrajectory.getEndTime()) <= 0
                        ).collect(Collectors.toList());
                tempPoints.forEach(e -> e.setTraId(traId));

                if (tempPoints.size() > 0){
                    taxiTrajectory.setStartLng(tempPoints.get(0).getLng());
                    taxiTrajectory.setStartLat(tempPoints.get(0).getLat());
                    taxiTrajectory.setEndLng(tempPoints.get(tempPoints.size() - 1).getLng());
                    taxiTrajectory.setEndLat(tempPoints.get(tempPoints.size() - 1).getLat());
                }

                double distanceCal = 0D;
                for (int i = 1; i < tempPoints.size(); i ++) {
                    distanceCal += camTrajectoryService.GetDistance(
                            tempPoints.get(i - 1).getLng(), tempPoints.get(i - 1).getLat(),
                            tempPoints.get(i).getLng(), tempPoints.get(i).getLat());
                }
                taxiTrajectory.setDistanceCal(distanceCal / 1000);
            }

            List<TrajectoryOut> trajectoryList = trajectoryInList.stream().map(TrajectoryIn::convertToTaxiTrajectoryOut).collect(Collectors.toList());
            CsvWriter writer = CsvUtil.getWriter(traFolder+"/"+ carNumber + ".csv", CharsetUtil.CHARSET_UTF_8);
            writer.writeBeans(trajectoryList);
            writer.close();

            List<GpsPointOut> pointList = pointInList.stream().map(GpsPointIn::convertToGpsPointOut).collect(Collectors.toList());
            writer = CsvUtil.getWriter(pointFolder+"/" + carNumber + ".csv", CharsetUtil.CHARSET_UTF_8);
            writer.writeBeans(pointList);
            writer.close();

            logger.info("处理完成" + carNumber);
        }

        File traDir = new File(traFolder);
        File[] traFiles = traDir.listFiles();
        Set<String> existCar = taxiTrajectoryMapper.getCarFromStat();
        for (File traFile : traFiles) {
            String carNumber = traFile.getName().split("\\.")[0];
            if (!existCar.contains(carNumber) && carNumberList.contains(carNumber)){
                String cmd = "cat "+ traFolder + "/" + traFile.getName() + " | clickhouse-client --date_time_input_format best_effort --query=\"INSERT INTO gps_trajectory_stat_1 FORMAT CSVWithNames\"";
                this.executeCmd(cmd);
            }
        }

        File pointDir = new File(pointFolder);
        File[] pointFiles = pointDir.listFiles();
        existCar = taxiTrajectoryMapper.getCarFromPoint();
        for (File pointFile : pointFiles) {
            String carNumber = pointFile.getName().split("\\.")[0];
            if (!existCar.contains(carNumber) && carNumberList.contains(carNumber)){
                String cmd = "cat "+ pointFolder + "/" + pointFile.getName() + " | clickhouse-client --date_time_input_format best_effort --query=\"INSERT INTO gps_points_1 FORMAT CSVWithNames\"";
                this.executeCmd(cmd);
            }
        }
    }

    public void executeCmd(String cmd) {
        try {

            // 执行脚本文件
            logger.info("开始执行命令:" + cmd);
            //主要在这步写入后调用命令
            String[] cmds = new String[]{"sh","-c",cmd};
            Process proc = Runtime.getRuntime().exec(cmds);
            proc.waitFor();
            try (
                 BufferedReader read =
                         new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = read.readLine()) != null) {
                    logger.info(line);
                }
            }
            logger.info("执行结束:" + cmd);
        } catch (Exception e) {
            logger.error("failed", e);
        }
    }

}
