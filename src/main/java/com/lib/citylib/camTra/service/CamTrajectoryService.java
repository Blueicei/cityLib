package com.lib.citylib.camTra.service;


import com.lib.citylib.camTra.dto.CamFlowDto;
import com.lib.citylib.camTra.dto.CityFlowDto;
import com.lib.citylib.camTra.dto.TableProcessDto;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.QueryCamFLow;
import com.lib.citylib.camTra.query.QueryDataSource;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.utils.GPSUtil;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CamTrajectoryService {
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private ReplaceTableInterceptor replaceTableInterceptor;

    public QueryDataSource changeDataSource(String tableName){
        String oldTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        QueryDataSource queryDataSource = new QueryDataSource();
        queryDataSource.setOldTableName(oldTableName);
        queryDataSource.setNewTableName(tableName);
        return queryDataSource;
    }

    public QueryGenerateResult generateTra(TableProcessDto tableProcessDto){
        System.out.println(tableProcessDto);
        String tableName = tableProcessDto.getTableName();
        List<String> carTypeList = tableProcessDto.getCarType();
        List<String> carNumberList = tableProcessDto.getCarNumber();
        String tempTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberList();
        int carCount = 0;
        int traCount = 0;
        for (CarInfo car : carList) {
            String carNumber = car.getCarNumber();
            String carType = car.getCarType();
            if (carTypeList.size() > 0 && !carTypeList.contains(carType)) {
                continue;
            }
            if (carNumberList.size() > 0) {
                Boolean isFilter = true;
                for (String carNumberFilter : carNumberList) {
                    if (carNumberFilter.equals("山东省济南市内") && carNumber.contains("鲁A")) {
                        isFilter = false;
                    }
                    if (carNumberFilter.equals("山东省济南市外") && (carNumber.contains("鲁") && !carNumber.contains("鲁A"))) {
                        isFilter = false;
                    }
                    if (carNumberFilter.equals("其他省份") && carNumber.contains("鲁")) {
                        isFilter = false;
                    }
                }
                if (isFilter) {
                    continue;
                }
            }
            try{ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                List<CamTrajectory> camPointList = camTrajectoryMapper.selectAllByCarNumber(carNumber);
                if (camPointList.size() == 0) {
                    continue;
                }
                if (tableProcessDto.getTraCut().equals(null))
                {
                    tableProcessDto.setTraCut(30l);
                }
                if (tableProcessDto.getPointNumber().equals(null))
                {
                    tableProcessDto.setPointNumber(0l);
                }if (tableProcessDto.getTraLength().equals(null))
                {
                    tableProcessDto.setTraLength(0l);
                }

                CarTrajectory carTra = new CarTrajectory(carNumber, carType, camPointList);
                DataSet<CamTrajectory> points = env.fromCollection(carTra.getPoints()).name("row-camtra-points");
                DataSet<CarTrajectory> newPoints = points.filter(new LonLatNotNullFilter()).
                        filter(new LonLatInRangeFilter(tableProcessDto.getFilterTraRange())).
                        sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        map(new PointListMap()).
                        reduce(new MergePoints()).
                        flatMap(new CutPointsToTrajectory(tableProcessDto.getTraCut())).
                        filter((new PointNumberFilter(tableProcessDto.getPointNumber()))).
                        map(new PointListToTraMap()).
                        filter(new TraLengthFilter(tableProcessDto.getTraLength())).
                        name("points-to-trajectory");
                List<CarTrajectory> newTraList = newPoints.collect();
                System.out.printf(newTraList.toString());
                FileWriteList("./out/"+tableName+"_"+carNumber+".csv", newTraList);
                carCount++;
                traCount = traCount + newTraList.size();
                break;
            }
            catch(Exception e){
                e.printStackTrace();
                replaceTableInterceptor.setTableName(tempTableName);
                return new QueryGenerateResult("failed", new Long((long)carCount), new Long((long)traCount));
            }
        }
        replaceTableInterceptor.setTableName(tempTableName);
        return new QueryGenerateResult("success", new Long((long)carCount), new Long((long)traCount));
    }

    public List<TableInfo> getTableNameList(List<String> tableNameList) {
        String tempTableName = replaceTableInterceptor.getTableName();
        List<TableInfo> tables = camTrajectoryMapper.getTableNameList(tableNameList);
        List<TableInfo> tempTables = new ArrayList<>();
        for (TableInfo table : tables) {
            replaceTableInterceptor.setTableName(table.getTableName());
            TableInfo tempTable = camTrajectoryMapper.getTableInfo();
            tempTable.setTableName(table.getTableName());
            tempTable.setLastModifyTime(table.getLastModifyTime());
            tempTables.add(tempTable);
        }
        replaceTableInterceptor.setTableName(tempTableName);
        return tempTables;
    }

    public List<String> getCarTypeList() {
        List<String> carTypeList = camTrajectoryMapper.getCarTypeList();
//        carTypeList.removeAll(Collections.singleton(null));
        return carTypeList;
    }

    public List<CamInfo> getAllCamInfo() {
        List<CamInfo> camInfoList = camTrajectoryMapper.getAllCamInfo();
        List<CamInfo> temp = new ArrayList<>();
        for (CamInfo camInfo : camInfoList) {
            double[] gps = GPSUtil.gps84_To_bd09(camInfo.getCamLat(), camInfo.getCamLon());
            CamInfo tempCamInfo = new CamInfo();
            tempCamInfo.setCamId(camInfo.getCamId());
            tempCamInfo.setCamAddress(camInfo.getCamAddress());
            tempCamInfo.setCamLat(gps[0]);
            tempCamInfo.setCamLon(gps[1]);
            temp.add(tempCamInfo);
        }
        return temp;
    }

    public List<QueryCamFLow> countCityFlow(CityFlowDto cityFlowDto) {
        List<String> camIds = cityFlowDto.getCamIds();
        Date startTime = cityFlowDto.getStartTime();
        Date endTime = cityFlowDto.getEndTime();
        Long timeGranularity = cityFlowDto.getTimeGranularity();
        Long timeInterval = (endTime.getTime() - startTime.getTime()) / 1000;
        if (timeGranularity >= timeInterval) {
            List<QueryCamFLow> queryCamFLowList = new ArrayList<>();
            for (String camId : camIds) {
                Long count = camTrajectoryMapper.countCityFlow(camId, startTime, endTime);
                QueryCamFLow tempQueryCamFLow = new QueryCamFLow();
                List<CamFlowDto> tempCamFlowList = new ArrayList<>();
                tempCamFlowList.add(new CamFlowDto(startTime, endTime, count));
                tempQueryCamFLow.setCamId(camId);
                tempQueryCamFLow.setCamFlows(tempCamFlowList);
            }
            return queryCamFLowList;
        } else {
            List<Tuple2<Date, Date>> timePairList = new ArrayList<>();
            Date tempStartTime = new Date(startTime.getTime());
            while (tempStartTime.getTime() <= endTime.getTime()) {
                timePairList.add(new Tuple2<>(tempStartTime, new Date(tempStartTime.getTime() + +timeGranularity * 60000)));
                tempStartTime = new Date(tempStartTime.getTime() + (timeGranularity + 1) * 60000);
            }
            List<QueryCamFLow> queryCamFLowList = new ArrayList<>();
            for (String camId : camIds) {
                QueryCamFLow tempQueryCamFLow = new QueryCamFLow();
                tempQueryCamFLow.setCamId(camId);
                List<CamFlowDto> tempCamFlowList = new ArrayList<>();
                for (Tuple2<Date, Date> timePair : timePairList) {
                    Long count = camTrajectoryMapper.countCityFlow(camId, timePair.f0, timePair.f1);
                    tempCamFlowList.add(new CamFlowDto(timePair.f0, timePair.f1, count));
                }
                tempQueryCamFLow.setCamFlows(tempCamFlowList);
                queryCamFLowList.add(tempQueryCamFLow);
            }
            return queryCamFLowList;
        }
    }

    public List<CamTrajectory> listByCarNumber(String carNumber) {
        return camTrajectoryMapper.selectAllByCarNumber(carNumber);
    }

    public CarTrajectory listByCarNumberOrderInTimeRange(String carNumber, Date startTime, Date endtTime) {
        List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carNumber, startTime, endtTime);
        if (camTraList.size() > 0) {
            String carType = camTraList.get(0).getCarType();
            return new CarTrajectory(carNumber, carType, camTraList);
        }
        return new CarTrajectory(carNumber, new ArrayList<CamTrajectory>());
    }

    public void insert() throws IOException {
        File dir = new File("D:\\workspace_py\\TrajMatchV1\\data\\202102");
        File[] files = dir.listFiles();
        for (File fileName : files) {
            try (FileInputStream fis = new FileInputStream(fileName);
                 InputStreamReader isr = new InputStreamReader(fis,
                         StandardCharsets.UTF_8);
                 CSVReader reader = new CSVReader(isr)) {
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    String carNumber = nextLine[0].split("-")[0];
                    String carType = nextLine[0].split("-")[1];
                    String[] pointList = Arrays.copyOfRange(nextLine, 1, nextLine.length);
//                    System.out.format("%s\n", carNumber);
                    for (String point : pointList) {
                        String camId = point.split("-")[0];
                        String direction = point.split("-")[1].split("@")[0];
                        String photoTime = point.split("@")[1];
//                        System.out.format("%s %s %s\n", camId, direction, photoTime);
                        CamTrajectory camTra = new CamTrajectory();
                        camTra.setCarNumber(carNumber);
                        camTra.setCarType(carType);
                        camTra.setCamId(camId);
                        camTra.setDirection(direction);
                        camTra.setPhotoTime(new Date(Long.parseLong(photoTime)));
                        camTrajectoryMapper.insertAll(camTra);
                    }
                }
                System.exit(0);
            }
        }
    }

    public static double[] Mercator2lonLat(double mercatorX,double mercatorY)
    {
        double[] xy = new double[2];
        double x = mercatorX/20037508.34*180;
        double M_PI = Math.PI;
        double y = mercatorY/20037508.34*180;
        y= 180/M_PI*(2*Math.atan(Math.exp(y*M_PI/180))-M_PI/2);

        xy[0] = x;
        xy[1] = y;
        return xy;

    }

    public static class LonLatInRangeFilter implements FilterFunction<CamTrajectory> {
        private boolean rangeFilter;
        public LonLatInRangeFilter(boolean rangeFilter){
            this.rangeFilter = rangeFilter;
        }
        @Override
        public boolean filter(CamTrajectory camTrajectory) throws Exception {
            return !rangeFilter || (rangeInDefined(camTrajectory.getCamLon(), 116.85706169, 117.38795955) && rangeInDefined(camTrajectory.getCamLat(), 36.57828896, 36.78481367));
        }
        public boolean rangeInDefined(double current, double min, double max)
        {
            return Math.max(min, current) == Math.min(current, max);
        }


    }
    public static Boolean filterCarNumber(String carNumber, List<String> carNumberList) {
        if (carNumberList.size() > 0) {
            Boolean isFilter = true;
            for (String carNumberFilter : carNumberList) {
                if (carNumberFilter.equals("山东省济南市内") && carNumber.contains("鲁A")) {
                    isFilter = false;
                }
                if (carNumberFilter.equals("山东省济南市外") && (carNumber.contains("鲁") && !carNumber.contains("鲁A"))) {
                    isFilter = false;
                }
                if (carNumberFilter.equals("其他省份") && carNumber.contains("鲁")) {
                    isFilter = false;
                }
            }
            return isFilter;
        }
        return false;
    }

    public static class LonLatNotNullFilter implements FilterFunction<CamTrajectory> {

        @Override
        public boolean filter(CamTrajectory camTrajectory) throws Exception {
            return camTrajectory.getCamLon() != null && camTrajectory.getCamLat() != null;
        }
    }

    public static class PointListToTraMap implements MapFunction<List<CamTrajectory>, CarTrajectory> {

        @Override
        public CarTrajectory map(List<CamTrajectory> camTrajectories) throws Exception {
            String carNumber = camTrajectories.get(0).getCarNumber();
            String carType = camTrajectories.get(0).getCarType();
            Double distance = 0.0d;
            for (int i = 1; i < camTrajectories.size(); i++) {
                CamTrajectory beforePoint = camTrajectories.get(i - 1);
                CamTrajectory afterPoint = camTrajectories.get(i);
                distance += GetDistance(beforePoint.getCamLon(), beforePoint.getCamLat(), afterPoint.getCamLon(), afterPoint.getCamLat());
            }
            Date startTime = camTrajectories.get(0).getPhotoTime();
            Date endTime = camTrajectories.get(camTrajectories.size() - 1).getPhotoTime();
            Long timeInterval = (endTime.getTime() - startTime.getTime()) / 1000;
            return new CarTrajectory(carNumber, carType, camTrajectories, distance, startTime, endTime, timeInterval);
        }
    }

    public static class PointListMap implements MapFunction<CamTrajectory, List<CamTrajectory>> {

        @Override
        public List<CamTrajectory> map(CamTrajectory camTrajectory) throws Exception {
            List<CamTrajectory> tempPoints = new ArrayList<>();
            tempPoints.add(camTrajectory);
            return tempPoints;
        }
    }

    public static class MergePoints implements ReduceFunction<List<CamTrajectory>> {

        @Override
        public List<CamTrajectory> reduce(List<CamTrajectory> camTrajectories, List<CamTrajectory> t1) throws Exception {
            camTrajectories.add(t1.get(0));
            return camTrajectories;
        }
    }

    public static class PointNumberFilter implements FilterFunction<List<CamTrajectory>> {

        private Long pointNumber = 0l;

        public PointNumberFilter(Long pointNumber) {this.pointNumber = pointNumber;}

        @Override
        public boolean filter(List<CamTrajectory> camTrajectoryList) throws Exception {
            return camTrajectoryList.size() > this.pointNumber;
        }
    }
    public static class TraLengthFilter implements FilterFunction<CarTrajectory> {

        private Long traLength = 0l;

        public TraLengthFilter(Long traLength) {this.traLength = traLength;}

        @Override
        public boolean filter(CarTrajectory carTrajectory) throws Exception {
            return carTrajectory.getDistance() > this.traLength;
        }
    }

    public static class CutPointsToTrajectory implements FlatMapFunction<List<CamTrajectory>, List<CamTrajectory>> {
        private Long trajectoryCut = 0l; // 自定义间隔时间（单位：毫秒）

        public CutPointsToTrajectory(Long trajectoryCut) {
            this.trajectoryCut = trajectoryCut;
        }

        @Override
        public void flatMap(List<CamTrajectory> camTrajectories, Collector<List<CamTrajectory>> collector) throws Exception {
            List<CamTrajectory> tempPoints = new ArrayList<>();
            CamTrajectory beforePoint = camTrajectories.get(0);
            tempPoints.add(beforePoint);
            for (int i = 1; i < camTrajectories.size(); i++) {
                CamTrajectory afterPoint = camTrajectories.get(i);
                if ((afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime()) / (1000.0 * 60.0) > trajectoryCut) {
                    collector.collect(tempPoints);
                    tempPoints.clear();
                    tempPoints.add(afterPoint);
                } else {
                    tempPoints.add(afterPoint);
                }
                beforePoint = afterPoint;
            }
            collector.collect(tempPoints);
        }

    }

    public static <T> void FileWriteList(String path, List<T> list) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            for (T s : list) {
                bufferedWriter.write(s.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 转化为弧度(rad)
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * @param lon1 第一点的精度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的精度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位m
     */
    public static double GetDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
