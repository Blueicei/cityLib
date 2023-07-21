package com.lib.citylib;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lib.citylib.camTra.dto.CityFlowDto;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.QueryCamFLow;
import com.lib.citylib.camTra.query.QueryGenerateResult;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.lib.citylib.camTra.utils.DirectoryStructure;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.Test;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class CityLibApplicationTests {

    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private CamTrajectoryService camTrajectoryService;
    @Resource
    private ReplaceTableInterceptor replaceTableInterceptor;

    @Test
    void test1() {
        List<CamInfo> tables = camTrajectoryService.getAllCamInfo();
        for(CamInfo table : tables) {
            System.out.println(table);
        }
    }

    public static class MergeGroupPoints implements GroupReduceFunction<CamTrajectory, CarTrajectory>{

        private Long trajectoryCut = 30L;
        private Long traLength = 0L;
        private Long pointNumber = 0L;
        public MergeGroupPoints(Long trajectoryCut, Long traLength, Long pointNumber) {
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
            return new CarTrajectory(carNumber, carType, camTrajectoryList, distance, startTime, endTime, timeInterval, avgSpeed);
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
                if (c.getPoints().size() > pointNumber && c.getDistance() > traLength)
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

    @Test
    void test4() {
        List<String> carNumber = new ArrayList<>();
        carNumber.add("山东省济南市内");
//        carNumber.add("山东省济南市外");
        List<String> carType = new ArrayList<>();
        carType.add("小型汽车号牌");
        carType.add("小型新能源汽车号牌");
        Boolean filterTraRange = true;
//        List<CamTrajectory> camPointList = camTrajectoryMapper.getAllPointInCondition(carNumber, carType, filterTraRange);
        List<CarInfo> carList = camTrajectoryMapper.getCarNumberListInCondition(carNumber, carType);
        QueryGenerateResult queryGenerateResult = new QueryGenerateResult();
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
                        reduceGroup(new MergeGroupPoints(30L, 1000L, 3L)).
                        name("points-to-trajectory");
                List<CarTrajectory> newTraList = newPoints.collect();
                queryGenerateResult.update(newTraList);
                FileWriteList("./out/4.txt", newTraList, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writeObjectToJsonFile(queryGenerateResult,"./out/2.txt");
    }
    @Test
    void test5() throws IOException {
        File file = new File("./out/2.txt");
        String json =  FileUtils.readFileToString(file);
        QueryGenerateResult queryGenerateResult = JSON.parseObject(json, QueryGenerateResult.class);
        System.out.println(queryGenerateResult.toString());
    }


    @Test
    void test2() {
        replaceTableInterceptor.setTableName("camtrajectory");
        List<CarInfo> tables = camTrajectoryMapper.getCarNumberList();
        for(CarInfo table : tables) {
            System.out.println(table);
        }
    }

    @Test
    void testFile() {
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        DirectoryStructure d = new DirectoryStructure();
        d.scan("D:\\CodeSearchNet");
        stringObjectHashMap.put("res", "[" + d.getBuf() + "]");
        System.out.println(stringObjectHashMap.toString());
        JSONObject jsonObject =  JSONObject.parseObject(d.getBuf().toString());
        System.out.println(jsonObject.toString());
    }
    @Test
    void testCarType() {
//        replaceTableInterceptor.setTableName("camtrajectory_copy");
//        List<String> tables = camTrajectoryService.getCarTypeList();
//        for(String table : tables) {
//            System.out.println(table);
//        }
//
        replaceTableInterceptor.setTableName("camtrajectory");
        List<String> tables1 = camTrajectoryService.getCarTypeList();
        for(String table : tables1) {
            System.out.println(table);
        }
    }

    @Test
    void testTableName() {
//        List<String> nameList = new ArrayList<>();
//        nameList.add("camtrajectory");
//        nameList.add("camtrajectory_copy");
//        List<TableInfo> tables = camTrajectoryMapper.getTableNameList(nameList);
        List<TableInfo> tables = camTrajectoryMapper.getTableNameList(null);
        for(TableInfo table : tables) {
            replaceTableInterceptor.setTableName(table.getTableName());
            TableInfo tempTable = camTrajectoryMapper.getTableInfo();
            tempTable.setTableName(table.getTableName());
            tempTable.setLastModifyTime(table.getLastModifyTime());
            System.out.println(tempTable);
        }

    }

    @Test
    void testCamTra() throws IOException {
//        CamTrajectory camTra = new CamTrajectory();
//        camTra.setCarNumber("鲁AS599D");
//        camTra.setCamId("3701033109");
//        camTra.setDirection("由东向西");
//        camTra.setPhotoTime(new Date(Long.parseLong("1612137859000")));
//        camTrajectoryMapper.insertAll(camTra);
//        camTrajectoryService.insert();
        List<CamTrajectory> camTraList = camTrajectoryMapper.selectAllByCarNumber("鲁AS599D-小型汽车号牌");
        for(CamTrajectory canTra : camTraList) {
            System.out.printf(canTra.toString());
        }
    }
    @Test
    void testCityFlow() throws ParseException {
        CityFlowDto cityFlowDto = new CityFlowDto();
        List<String> camIds = new ArrayList<>();
        camIds.add("3701126493");
        camIds.add("3701126286");
        cityFlowDto.setCamIds(camIds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2021-2-1 0:00:00");
        Date endTime = sdf.parse("2021-2-1 20:00:00");
        cityFlowDto.setStartTime(startTime);
        cityFlowDto.setEndTime(endTime);
        cityFlowDto.setTimeGranularity(new Long(30));
        List<QueryCamFLow> queryCamFLowList = camTrajectoryService.countCityFlow(cityFlowDto);
        for(QueryCamFLow queryCamFLow : queryCamFLowList) {
            System.out.println(queryCamFLow.toString());
        }
    }

    @Test
    void  testStreamFlink() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2021-2-1 0:00:00");
        Date endTime = sdf.parse("2021-2-1 20:00:00");
        List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange("鲁A0000999046", startTime, endTime);
        for(CamTrajectory canTra : camTraList) {
            System.out.printf(canTra.toString());
        }
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<CamTrajectory> points = env.fromCollection(camTraList).name("row-camtra-points");
        DataStream<CarTrajectory> camTra = points.keyBy(CamTrajectory::getCarNumber).process(new CamTraPreProcess()).name("pre-process");
//        DataStream<CarTrajectory> camTra = points.map(new Enrichment()).keyBy(CarTrajectory::getCarNumber).reduce(new AggregatePoints());
//        camTra.addSink(StreamingFileSink.forRowFormat(new Path("./out"),new SimpleStringEncoder("UTF-8")).build());
        camTra.print();
        env.execute("sink test job");
    }

    @Test
    void  testBatchFlink() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2021-2-1 0:00:00");
        Date endTime = sdf.parse("2021-2-1 20:00:00");
        CarTrajectory carTra = camTrajectoryService.listByCarNumberOrderInTimeRange("鲁A0000999046", startTime, endTime);
        String carNumber = carTra.getCarNumber();
        String carType = carTra.getCarType();
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<CamTrajectory> points = env.fromCollection(carTra.getPoints()).name("row-camtra-points");
//        DataSet<CarTrajectory> camTra = points.map(new Enrichment()).groupBy(CarTrajectory::getCarNumber).reduce((CarTrajectory t1, CarTrajectory t2)-> {
//            return t1.mergePoint(t2);
//        });
//        camTra.print();
        DataSet<CarTrajectory> newPoints = points.filter(new LonLatNotNullFilter()).
                sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                map(new PointListMap()).
                reduce(new MergePoints()).
                flatMap(new CutPointsToTrajectory()).
                filter((List<CamTrajectory> l1) -> {return l1.size() > 3;}).
                map(new PointListToTraMap()).
                filter((CarTrajectory c) -> {return c.getCarNumber().contains("鲁A");}).
                filter((CarTrajectory c) -> {return ArrayUtils.contains(new String[]{"小型汽车号牌", "小型新能源汽车号牌"}, c.getCarType());}).
                filter((CarTrajectory c) -> {return c.getDistance() > 2000;}).
                name("points-to-trajectory");
//        newPoints.print();
        List<CarTrajectory> newTraList = newPoints.collect();
        System.out.printf(newTraList.toString());
        FileWriteList("./out/1.txt", newTraList, false);

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
            for (int i = 1; i < camTrajectories.size(); i++){
                CamTrajectory beforePoint = camTrajectories.get(i-1);
                CamTrajectory afterPoint = camTrajectories.get(i);
                distance += GetDistance(beforePoint.getCamLon(), beforePoint.getCamLat(), afterPoint.getCamLon(), afterPoint.getCamLat());
            }
            Date startTime = camTrajectories.get(0).getPhotoTime();
            Date endTime = camTrajectories.get(camTrajectories.size()-1).getPhotoTime();
            Long timeInterval = (endTime.getTime() - startTime.getTime()) / 1000;
            return new CarTrajectory(carNumber, carType, camTrajectories,distance, startTime, endTime, timeInterval);
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
    public static class CutPointsToTrajectory implements FlatMapFunction<List<CamTrajectory>, List<CamTrajectory>>{

        @Override
        public void flatMap(List<CamTrajectory> camTrajectories, Collector<List<CamTrajectory>> collector) throws Exception {
            List<CamTrajectory> tempPoints = new ArrayList<>();
            CamTrajectory beforePoint = camTrajectories.get(0);
            tempPoints.add(beforePoint);
            for (int i = 1; i < camTrajectories.size(); i++){
                CamTrajectory afterPoint = camTrajectories.get(i);
                if ((afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime()) / (1000 * 60) > 30) {
                    collector.collect(tempPoints);
                    tempPoints.clear();
                    tempPoints.add(afterPoint);
                }
                else {
                    tempPoints.add(afterPoint);
                }
                beforePoint = afterPoint;
            }
            collector.collect(tempPoints);
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

    public static class Enrichment implements MapFunction<CamTrajectory, CarTrajectory> {

        @Override
        public CarTrajectory map(CamTrajectory camTrajectory) throws Exception {
            return new CarTrajectory(camTrajectory.getCarNumber(), camTrajectory);
        }
    }
    public static class AggregatePoints implements ReduceFunction<CarTrajectory> {
        @Override
        public CarTrajectory reduce(CarTrajectory carTrajectory, CarTrajectory t1) throws Exception {
            return carTrajectory.mergePoint(t1);
        }
    }
    private static double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 转化为弧度(rad)
     * */
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    /**
     * @param lon1 第一点的精度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的精度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位m
     * */
    public static double GetDistance(double lon1,double lat1,double lon2, double lat2) {
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
