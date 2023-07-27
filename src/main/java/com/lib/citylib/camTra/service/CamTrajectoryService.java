package com.lib.citylib.camTra.service;


import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lib.citylib.camTra.dto.*;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.*;
import com.lib.citylib.camTra.utils.GPSUtil;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import com.opencsv.CSVReader;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
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

    public List<List<QueryCamCount>> getHotMapInfoByTimeAndCut(StartToEndTime startToEndTime) {
        Date currentDate = startToEndTime.getStartTime();
        Date endDate = startToEndTime.getEndTime();

        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        while (calendar.before(endCalendar) || calendar.equals(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        Date firstDate = datesInRange.get(0);
        calendar.setTime(firstDate);
        calendar.add(Calendar.DATE, 1);
        Date secondDate = calendar.getTime();
        List<List<QueryCamCount>> queryCamCountList = new ArrayList<>();
        while(datesInRange.get(0).compareTo(secondDate)<0){
            List<StartToEndTime> startToEndTimeList = new ArrayList<>();
            List<Date> newStartDateList = new ArrayList<>();
            for(Date tempDate : datesInRange){
                Calendar rightNow = Calendar.getInstance();
                rightNow.setTime(tempDate);
                rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
                Date nextDate = rightNow.getTime();
                StartToEndTime tempTimePair = new StartToEndTime();
                tempTimePair.setStartTime(tempDate);
                tempTimePair.setEndTime(nextDate);
                startToEndTimeList.add(tempTimePair);
                newStartDateList.add(nextDate);
            }
            queryCamCountList.add(camTrajectoryMapper.listCamCountInRange(startToEndTimeList));
            datesInRange = newStartDateList;
        }

//        List<List<QueryCamCount>> queryCamCountList = new ArrayList<>();
//        while(currentDate.compareTo(endDate)<0){
//            Calendar rightNow = Calendar.getInstance();
//            rightNow.setTime(currentDate);
//            rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
//            Date nextDate = rightNow.getTime();
//            if(nextDate.compareTo(endDate)<0){
//                List<QueryCamCount> camInfoCounts = camTrajectoryMapper.listCamCount(currentDate,nextDate);
//                queryCamCountList.add(camInfoCounts);
//            }else {
//                List<QueryCamCount> camInfoCounts = camTrajectoryMapper.listCamCount(currentDate,endDate);
//                queryCamCountList.add(camInfoCounts);
//            }
//            currentDate = nextDate;
//        }
        List<CamInfo> camInfoList = camTrajectoryMapper.getAllCamInfo();
        Map<String,CamInfo> camInfoMap = new HashMap<>();
        for(CamInfo c:camInfoList){
            double[] gps = GPSUtil.gps84_To_bd09(c.getCamLat(), c.getCamLon());
            c.setCamLat(gps[0]);
            c.setCamLon(gps[1]);
            camInfoMap.put(c.getCamId(),c);
        }
        List<List<QueryCamCount>> newList = new ArrayList<>();
        for(List<QueryCamCount> queryCamCount : queryCamCountList){
            List<QueryCamCount> list = new ArrayList<>();
            for(QueryCamCount q:queryCamCount){
                q.setCamInfo(camInfoMap.get(q.getCamId()));
                list.add(q);
            }
            newList.add(list);
        }
        return newList;
    }

    public List<QueryCamCount> getHotMapInfoByTime(StartToEndTime startToEndTime) {
        List<QueryCamCount> queryCamCountList = camTrajectoryMapper.listCamCount(startToEndTime.getStartTime(),startToEndTime.getEndTime());
        List<CamInfo> camInfoList = camTrajectoryMapper.getAllCamInfo();
        Map<String,CamInfo> camInfoMap = new HashMap<>();
        for(CamInfo c:camInfoList){
            double[] gps = GPSUtil.gps84_To_bd09(c.getCamLat(), c.getCamLon());
            c.setCamLat(gps[0]);
            c.setCamLon(gps[1]);
            camInfoMap.put(c.getCamId(),c);
        }
        List<QueryCamCount> newList = new ArrayList<>();
        for(QueryCamCount queryCamCount : queryCamCountList){
            queryCamCount.setCamInfo(camInfoMap.get(queryCamCount.getCamId()));
            newList.add(queryCamCount);
        }
        return newList;
    }

    public List<QueryClusterFlow> getClusterFlow(ClusterFlowDto clusterFlowDto){

        return null;
    }

    public List<QueryCamCountByCar> getCamCountByCar(String carNumber){
        List<QueryCamCountByCar> queryCamCountByCarList = camTrajectoryMapper.listCamCountByCar(carNumber, null, null);
        List<QueryCamCountByCar> newCamCountByCarList = new ArrayList<>();
        for(QueryCamCountByCar queryCamCountByCar : queryCamCountByCarList){
            CamInfo camInfo = camTrajectoryMapper.getCamInfo(queryCamCountByCar.getCamId());
            double[] gps = GPSUtil.gps84_To_bd09(camInfo.getCamLat(), camInfo.getCamLon());
            camInfo.setCamLat(gps[0]);
            camInfo.setCamLon(gps[1]);
            queryCamCountByCar.setCamInfo(camInfo);
            newCamCountByCarList.add(queryCamCountByCar);
        }
        return newCamCountByCarList;
    }

    public List<CarTrajectory> getTraByCar(String carNumber) throws Exception {
        List<CamTrajectory> camPointList = camTrajectoryMapper.selectAllByCarNumber(carNumber);
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<CamTrajectory> points = env.fromCollection(camPointList).name("row-camtra-points");
        DataSet<CarTrajectory> newPoints = points.
                distinct().
                groupBy(CamTrajectory::getCarNumber).
                sortGroup(CamTrajectory::getPhotoTime, Order.ASCENDING).
                reduceGroup(new MergeGroupPoints(30L, 1000L, 3L, true)).
                name("points-to-trajectory");
        List<CarTrajectory> newTraList = newPoints.collect();
        List<CarTrajectory> tempTraList = new ArrayList<>();
        for (CarTrajectory carTrajectory : newTraList){
            List<CamTrajectory> tempPointList = new ArrayList<>();
            for(CamTrajectory camTrajectory : carTrajectory.getPoints()){
                double[] gps = GPSUtil.gps84_To_bd09(camTrajectory.getCamLat(), camTrajectory.getCamLon());
                camTrajectory.setCamLat(gps[0]);
                camTrajectory.setCamLon(gps[1]);
                tempPointList.add(camTrajectory);
            }
            carTrajectory.setPoints(tempPointList);
            tempTraList.add(carTrajectory);
        }
        return tempTraList;
    }

    public QueryDataSource changeDataSource(String tableName) {
        String oldTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        QueryDataSource queryDataSource = new QueryDataSource();
        queryDataSource.setOldTableName(oldTableName);
        queryDataSource.setNewTableName(tableName);
        return queryDataSource;
    }

    public QueryGenerateResult generateTra(TableProcessDto tableProcessDto) {
        String tableName = tableProcessDto.getTableName();
        List<String> carTypeList = tableProcessDto.getCarType();
        List<String> carNumberList = tableProcessDto.getCarNumber();
        Boolean filterTraRange = tableProcessDto.getFilterTraRange();
        String tempTableName = replaceTableInterceptor.getTableName();
        replaceTableInterceptor.setTableName(tableName);
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberListInCondition(carNumberList, carTypeList);
        QueryGenerateResult queryGenerateResult = new QueryGenerateResult();
        queryGenerateResult.setTableProcess(tableProcessDto);
        int segmentSize = 1000;
        for (int i = 0; i < carList.size(); i += segmentSize) {
            int endIndex = Math.min(i + segmentSize, carList.size());
            List<CarInfo> segment = carList.subList(i, endIndex);
            List<CamTrajectory> camPointList = camTrajectoryMapper.getPartialCarPointInCondition(segment, filterTraRange);
            try {
                ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                DataSet<CamTrajectory> points = env.fromCollection(camPointList).name("row-camtra-points");
                DataSet<CarTrajectory> newPoints = points.
                        distinct().
                        groupBy(CamTrajectory::getCarNumber).
                        sortGroup(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        reduceGroup(new MergeGroupPoints(tableProcessDto.getTraCut(), tableProcessDto.getTraLength(), tableProcessDto.getPointNumber(), true)).
                        name("points-to-trajectory");
                List<CarTrajectory> newTraList = newPoints.collect();
                queryGenerateResult.update(newTraList);
                FileWriteList("./out/trajectory_" + tableName + ".csv", newTraList, true);
            } catch (Exception e) {
                e.printStackTrace();
                replaceTableInterceptor.setTableName(tempTableName);
                queryGenerateResult.setMsg("error");
                return queryGenerateResult;
            }
        }
        writeObjectToJsonFile(queryGenerateResult,"./out/statistics_" + tableName + ".json");
        replaceTableInterceptor.setTableName(tempTableName);
        queryGenerateResult.setMsg("success");
        return queryGenerateResult;
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

    public static <T> void FileWriteList(String path, List<T> list, Boolean append) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(path, append), "UTF-8"));
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

    public static class MergeGroupPoints implements GroupReduceFunction<CamTrajectory, CarTrajectory>{

        private Long trajectoryCut = 30L;
        private Long traLength = 0L;
        private Long pointNumber = 0L;
        private Boolean savePoint = true;
        public MergeGroupPoints(Long trajectoryCut, Long traLength, Long pointNumber, Boolean savePoint) {
            if(trajectoryCut != null)
            {
                this.trajectoryCut = trajectoryCut;
            }
            if(traLength != null) {
                this.traLength = traLength;
            }
            if (pointNumber != null)
            {
                this.pointNumber = pointNumber;
            }
            if (savePoint != null)
            {
                this.savePoint = savePoint;
            }
        }
        private CarTrajectory convertCamListToCarTra(List<CamTrajectory> camTrajectoryList){

            String carNumber = camTrajectoryList.get(0).getCarNumber();
            String carType = camTrajectoryList.get(0).getCarType();
            Double distance = 0.0d;
            for (int i = 1; i < camTrajectoryList.size(); i++) {
                CamTrajectory beforePoint = camTrajectoryList.get(i - 1);
                CamTrajectory afterPoint = camTrajectoryList.get(i);
                distance += GetDistance(beforePoint.getCamLon(), beforePoint.getCamLat(), afterPoint.getCamLon(), afterPoint.getCamLat());
            }
            Date startTime = camTrajectoryList.get(0).getPhotoTime();
            Date endTime = camTrajectoryList.get(camTrajectoryList.size() - 1).getPhotoTime();
            Long timeInterval = (endTime.getTime() - startTime.getTime()) / 1000;
            Double avgSpeed = ( distance / timeInterval)*3.6d;
            if(!this.savePoint)
                return new CarTrajectory(carNumber, carType, null, distance, startTime, endTime, timeInterval, avgSpeed, camTrajectoryList.size());
            else
                return new CarTrajectory(carNumber, carType, camTrajectoryList, distance, startTime, endTime, timeInterval, avgSpeed, camTrajectoryList.size());
        }
        @Override
        public void reduce(Iterable<CamTrajectory> iterable, Collector<CarTrajectory> collector) throws Exception {
            List<CamTrajectory> camTrajectories = new ArrayList<>();
            for(CamTrajectory c : iterable){
                camTrajectories.add(c);
            }
            List<CamTrajectory> tempPoints = new ArrayList<>();
            List<CarTrajectory> carTrajectoryList = new ArrayList<>();
            CamTrajectory beforePoint = camTrajectories.get(0);
            tempPoints.add(beforePoint);
            for (int i = 1; i < camTrajectories.size(); i++) {
                CamTrajectory afterPoint = camTrajectories.get(i);
                if ((afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime()) / (1000.0 * 60.0) > trajectoryCut) {
                    carTrajectoryList.add(this.convertCamListToCarTra(tempPoints));
                    tempPoints = new ArrayList<CamTrajectory>();
                    tempPoints.add(afterPoint);
                } else {
                    tempPoints.add(afterPoint);
                }
                beforePoint = afterPoint;
            }
            carTrajectoryList.add(this.convertCamListToCarTra(tempPoints));
            for(CarTrajectory c: carTrajectoryList){
                if (c.getPointNum()> pointNumber && c.getDistance() > traLength)
                {
                    collector.collect(c);
                }
            }
        }
    }
    public static void writeObjectToJsonFile(Object object, String filePath) {
        // 创建ObjectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 将对象转换为JSON字符串
            String jsonString = objectMapper.writeValueAsString(object);

            // 将JSON字符串写入文件
            objectMapper.writeValue(new File(filePath), object);

            System.out.println("对象已成功写入到JSON文件。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
