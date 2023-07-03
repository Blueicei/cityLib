package com.lib.citylib.camTra.service;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.Query.QueryCityFlowStats;
import com.lib.citylib.camTra.dto.*;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.Query.QueryVehicleAppearanceByCar;
import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.opencsv.CSVReader;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
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

    public List<CamTrajectory> listByCarNumber(String carNumber) {
        return camTrajectoryMapper.selectAllByCarNumber(carNumber);
    }

    public List<CamInfo> getAllCamInfo() {
        return camTrajectoryMapper.getAllCamInfo();
    }

    public List<CarTrajectory> listByCarNumberOrderInTimeRange(List<String> carNumber, Date startTime, Date endtTime) throws Exception {
        List<CarTrajectory> carTrajectories = new ArrayList<>();
        for (int i = 0; i < carNumber.size(); i++) {
            List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carNumber.get(i), startTime, endtTime);
            if (camTraList.size() > 0) {
                String carType = camTraList.get(0).getCarType();
                CarTrajectory carTrajectory = new CarTrajectory(carNumber.get(i), carType, camTraList);

                ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                DataSet<CamTrajectory> points = env.fromCollection(carTrajectory.getPoints()).name("row-camtra-points");

                //carTrajectories.add(carTrajectory);
                DataSet<CarTrajectory> newPoints = points.
                        filter(new LonLatNotNullFilter()).
                        sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                        map(new PointListMap()).
                        reduce(new MergePoints()).
                        flatMap(new CutPointsToTrajectory(30)).
                        filter((List<CamTrajectory> l1) -> {return l1.size() > 3;}).
                        map(new PointListToTraMap()).
                        name("points-to-trajectory");

                List<CarTrajectory> newTraList = newPoints.collect();
                carTrajectories.addAll(newTraList);
            }
        }

        return carTrajectories;
    }

    public List<CarTrajectory> listByCarNumberAndCamIdOrderInTimeRange(String carNumber, Date startTime, Date endtTime, List<String> camIds) throws Exception {
        List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carNumber, startTime, endtTime);
        List<CarTrajectory> carTrajectories = new ArrayList<>();
        if (camTraList.size() > 0) {
            String carType = camTraList.get(0).getCarType();

            CarTrajectory carTrajectory = new CarTrajectory(carNumber, carType, camTraList);

            ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
            DataSet<CamTrajectory> points = env.fromCollection(carTrajectory.getPoints()).name("row-camtra-points");

            //carTrajectories.add(carTrajectory);
            DataSet<CarTrajectory> newPoints = points.
                    filter(new LonLatNotNullFilter()).
                    sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                    map(new PointListMap()).
                    reduce(new MergePoints()).
                    flatMap(new CutPointsToTrajectory(30)).
                    filter((List<CamTrajectory> l1) -> {return l1.size() > 3;}).
                    map(new PointListToTraMap()).
                    filter((CarTrajectory c) -> {
                        for (CamTrajectory point : c.getPoints()) {
                            if (camIds.contains(point.getCamId())) {
                                return true;
                            }
                        }
                        return false;
                    }).
                    name("points-to-trajectory");

            List<CarTrajectory> newTraList = newPoints.collect();
            carTrajectories.addAll(newTraList);
        }
        return carTrajectories;
    }

    public List<QueryVehicleCountByCam> vehicleCountByCam(VehicleCountByCamDto vehicleCountByCamDto) throws Exception {
        List<String> allcamId = vehicleCountByCamDto.getCamIds();
        List<QueryVehicleCountByCam> queryVehicleCountByCams = new ArrayList<>();
        for (int i = 0; i < allcamId.size(); i++) {
            if (i == 0){
                List<String> carNumbers = camTrajectoryMapper.vehicleCountByCam(allcamId.get(i),vehicleCountByCamDto.getStartTime(),vehicleCountByCamDto.getEndTime());
//                System.out.println(carNumbers);
                Map<String, Integer> occurrences = new HashMap<>();
                for (String element : carNumbers) {
                    occurrences.put(element, occurrences.getOrDefault(element, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
                    String element = entry.getKey();
                    int count = entry.getValue();
                    queryVehicleCountByCams.add(new QueryVehicleCountByCam(element,allcamId.get(i),count));
                }
            }
            else {
                List<String> carNumbers = camTrajectoryMapper.vehicleCountByCam(allcamId.get(i),vehicleCountByCamDto.getStartTime(),vehicleCountByCamDto.getEndTime());
                Map<String, Integer> occurrences = new HashMap<>();
                for (String element : carNumbers) {
                    occurrences.put(element, occurrences.getOrDefault(element, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
                    String element = entry.getKey();
                    int count = entry.getValue();
                    int flag = 0;
                    for (int j = 0; j < queryVehicleCountByCams.size(); j++) {
                        if (queryVehicleCountByCams.get(j).getCarNumber().equals(element)){
                            queryVehicleCountByCams.get(j).addCount(count);
                            queryVehicleCountByCams.get(j).addCam(allcamId.get(i));
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        queryVehicleCountByCams.add(new QueryVehicleCountByCam(element,allcamId.get(i),count));
                    }
                }
            }


        }
        return queryVehicleCountByCams;
    }

    public List<QueryVehicleAppearanceByCar> vehicleAppearanceByCar(VehicleAppearanceByCarDto vehicleAppearanceByCarDto) throws Exception {
        List<QueryVehicleAppearanceByCar> queryVehicleAppearanceByCars = new ArrayList<>();
        List<String> allCarNumbers = vehicleAppearanceByCarDto.getCarNumbers();
        Date startTime = vehicleAppearanceByCarDto.getStartTime();
        Date endTime = vehicleAppearanceByCarDto.getEndTime();
        for (int i = 0; i < allCarNumbers.size(); i++) {
            List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(allCarNumbers.get(i), startTime, endTime);
            int count = camTraList.size();
            queryVehicleAppearanceByCars.add(new QueryVehicleAppearanceByCar(allCarNumbers.get(i),count));
        }
        return queryVehicleAppearanceByCars;
    }


    /**
     * 6.25
     * @param camCountByCarDto
     * @return List<QueryCamCountByCar>
     * @throws Exception
     */


    public List<QueryCamCountByCar> listCamCountByCar(CamCountByCarDto camCountByCarDto){
        String carNumber = camCountByCarDto.getCarNumber();
        List<QueryCamCountByCar> list = camTrajectoryMapper.listCamCountByCar(carNumber,camCountByCarDto.getStartTime(),camCountByCarDto.getEndTime());
        return list;
    }


    //可优化性能？
    public List<CarTrajectory> listByTrajectoryDto(TrajectoryDto trajectoryDto) throws Exception {
        if (trajectoryDto.getCarTypes().isEmpty())
            return new ArrayList<CarTrajectory>();
        List<CarTrajectory> carTrajectories = new ArrayList<>();
//        for (int i = 0; i < trajectoryDto.getCarNumbers().size(); i++) {
            for (int j = 0; j < trajectoryDto.getCarNumbers().size(); j++) {
                List<CamTrajectory> camTrajectories = camTrajectoryMapper.listByTrajectoryDto(
                        trajectoryDto.getCarNumbers().get(j),
//                        trajectoryDto.getCarTypes().get(j),
                        trajectoryDto.getStartTime(),
                        trajectoryDto.getEndTime()
                );

                if (camTrajectories.size() > 0) {
                    String carType = camTrajectories.get(0).getCarType();
                    String carNumber = camTrajectories.get(0).getCarNumber();
                    CarTrajectory carTrajectory = new CarTrajectory(carNumber, carType, camTrajectories);
//                    System.out.println(carTrajectory.getPoints().get(0).getCamId());
//                    System.out.println(carTrajectory.getPoints());

                    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
                    DataSet<CamTrajectory> points = env.fromCollection(carTrajectory.getPoints()).name("row-camtra-points");

                    //carTrajectories.add(carTrajectory);
                    DataSet<CarTrajectory> newPoints = points.
                            filter((CamTrajectory c) -> trajectoryDto.getCarTypes().contains(c.getCarType())).
                            filter(new LonLatNotNullFilter()).
                            sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                            map(new PointListMap()).
                            reduce(new MergePoints()).
                            flatMap(new CutPointsToTrajectory(trajectoryDto.getTrajectoryCut())).
                            filter((List<CamTrajectory> l1) -> {return l1.size() > 3;}).
                            map(new PointListToTraMap()).
                            filter((CarTrajectory c) -> {
                                for (CamTrajectory point : c.getPoints()) {
                                    if (trajectoryDto.getCamIds().contains(point.getCamId())) {
                                        return true;
                                    }
                                }
                                return false;
                            }).
                            name("points-to-trajectory");

                    List<CarTrajectory> newTraList = newPoints.collect();
                    System.out.printf(newTraList.toString());
                    carTrajectories.addAll(newTraList);
                }

            }


        return carTrajectories;
    }

    public List<QueryCityFlowStats> cityFlowStats(VehicleCountByCamDto vehicleCountByCamDto){
        List<String> camids = vehicleCountByCamDto.getCamIds();
        Date startTime = vehicleCountByCamDto.getStartTime();
        Date endTime = vehicleCountByCamDto.getEndTime();
        List<QueryCityFlowStats> queryCityFlowStatsList = new ArrayList<>();
        for (int i = 0; i < camids.size(); i++) {
            QueryCityFlowStats queryCityFlowStats = new QueryCityFlowStats();
            queryCityFlowStats.setCamId(camids.get(i));
            List<CityFlowStats> list = camTrajectoryMapper.cityFlowStats(camids.get(i), startTime, endTime);
            int totalFlow = 0;
            for (CityFlowStats stat : list) {
                totalFlow += stat.getFlow();
            }
            queryCityFlowStats.setTotalFlow(totalFlow);
            queryCityFlowStats.setCityFlowStats(list);
            queryCityFlowStatsList.add(queryCityFlowStats);
        }
        return queryCityFlowStatsList;

    }

    public List<CompareVehicleStats> compareVehiclesStats(ForeignVehicleStatsDto foreignVehicleStatsDto){
        List<String> camids = foreignVehicleStatsDto.getCamIds();
        Date startTime = foreignVehicleStatsDto.getStartTime();
        Date endTime = foreignVehicleStatsDto.getEndTime();
        List<CompareVehicleStats> compareVehicleStatsList = new ArrayList<>();
        int granularity = foreignVehicleStatsDto.getGranularity();
        for (int i = 0; i < camids.size(); i++) {

            List<CamTrajectory> camTrajectories = camTrajectoryMapper.compareVehiclesStats(camids.get(i),startTime,endTime);

            List<SliceCamTrajectoryCompare> dividedLists = splitCompare(camTrajectories, startTime, endTime, granularity);
            for (SliceCamTrajectoryCompare timeSlice : dividedLists) {
                timeSlice.setCount(timeSlice.getTrajectories().size());
            }
            compareVehicleStatsList.add(new CompareVehicleStats(camids.get(i),dividedLists));
            System.out.println(new CompareVehicleStats(camids.get(i),dividedLists));
        }

        return compareVehicleStatsList;

    }

    public List<ForeignVehicleStats> foreignVehiclesStats(ForeignVehicleStatsDto foreignVehicleStatsDto){
        List<String> camids = foreignVehicleStatsDto.getCamIds();
        Date startTime = foreignVehicleStatsDto.getStartTime();
        Date endTime = foreignVehicleStatsDto.getEndTime();
        List<ForeignVehicleStats> foreignVehicleStatsList = new ArrayList<>();
        int granularity = foreignVehicleStatsDto.getGranularity();
        for (int i = 0; i < camids.size(); i++) {

            List<CamTrajectory> camTrajectories = camTrajectoryMapper.foreignVehiclesStats(camids.get(i),startTime,endTime);

            List<SliceCamTrajectoryForeign> dividedLists = splitForeign(camTrajectories, startTime, endTime, granularity);
            int provincialCount = 0;
            int nonProvincialCount = 0;

            for (SliceCamTrajectoryForeign timeSlice : dividedLists) {
                for (CamTrajectory trajectory : timeSlice.getTrajectories()) {
                    String carNumber = trajectory.getCarNumber();
                    if (carNumber != null && carNumber.startsWith("鲁")) {
                        provincialCount++;
                    } else {
                        nonProvincialCount++;
                    }
                }
                timeSlice.setProvincialCount(provincialCount);
                timeSlice.setNonProvincialCount(nonProvincialCount);
            }
            foreignVehicleStatsList.add(new ForeignVehicleStats(camids.get(i),dividedLists));
            System.out.println(new ForeignVehicleStats(camids.get(i),dividedLists));
        }

        return foreignVehicleStatsList;

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
    public static class CutPointsToTrajectory implements FlatMapFunction<List<CamTrajectory>, List<CamTrajectory>> {
        private int trajectoryCut = 0; // 自定义间隔时间（单位：毫秒）

        public CutPointsToTrajectory(int trajectoryCut) {
            this.trajectoryCut = trajectoryCut;
        }
        @Override
        public void flatMap(List<CamTrajectory> camTrajectories, Collector<List<CamTrajectory>> collector) throws Exception {
//            System.out.println(this.trajectoryCut);
            List<CamTrajectory> tempPoints = new ArrayList<>();
            CamTrajectory beforePoint = camTrajectories.get(0);
            tempPoints.add(beforePoint);
            for (int i = 1; i < camTrajectories.size(); i++){
                CamTrajectory afterPoint = camTrajectories.get(i);
//                System.out.println(afterPoint.getPhotoTime().getTime());
//                System.out.println(beforePoint.getPhotoTime().getTime());
//                System.out.println(afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime());
                if ((afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime()) / (1000.0 * 60.0) > trajectoryCut) {
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
    public static List<SliceCamTrajectoryForeign> splitForeign(List<CamTrajectory> camTrajectories, Date startTime, Date endTime, int granularity) {
        // 计算时间段的总毫秒数
        long totalTimeRange = endTime.getTime() - startTime.getTime();

        // 计算每个时间片段的毫秒数
        long timeSlice = granularity * 60 * 1000; // 将粒度转换为毫秒

        // 计算时间片段数量
        int numSlices = (int) Math.ceil((double) totalTimeRange / timeSlice);

        // 创建用于存储分割后部分的列表
        List<SliceCamTrajectoryForeign> dividedLists = new ArrayList<>(numSlices);

        // 初始化分割后部分的列表
        for (int i = 0; i < numSlices; i++) {
            Date sliceStartTime = new Date(startTime.getTime() + i * timeSlice);
            Date sliceEndTime = new Date(sliceStartTime.getTime() + timeSlice);
            SliceCamTrajectoryForeign timeSliceObj = new SliceCamTrajectoryForeign(sliceStartTime, sliceEndTime);
            dividedLists.add(timeSliceObj);
        }

        // 遍历每个轨迹点，将其分配到相应的时间片段列表
        for (CamTrajectory trajectory : camTrajectories) {
            Date photoTime = trajectory.getPhotoTime();

            // 计算轨迹点在时间片段中的索引
            int sliceIndex = (int) ((photoTime.getTime() - startTime.getTime()) / timeSlice);

            // 将轨迹点添加到相应的时间片段列表
            SliceCamTrajectoryForeign timeSliceObj = dividedLists.get(sliceIndex);
            timeSliceObj.getTrajectories().add(trajectory);
        }

        return dividedLists;
    }

    public static List<SliceCamTrajectoryCompare> splitCompare(List<CamTrajectory> camTrajectories, Date startTime, Date endTime, int granularity) {
        // 计算时间段的总毫秒数
        long totalTimeRange = endTime.getTime() - startTime.getTime();

        // 计算每个时间片段的毫秒数
        long timeSlice = granularity * 60 * 1000; // 将粒度转换为毫秒

        // 计算时间片段数量
        int numSlices = (int) Math.ceil((double) totalTimeRange / timeSlice);

        // 创建用于存储分割后部分的列表
        List<SliceCamTrajectoryCompare> dividedLists = new ArrayList<>(numSlices);

        // 初始化分割后部分的列表
        for (int i = 0; i < numSlices; i++) {
            Date sliceStartTime = new Date(startTime.getTime() + i * timeSlice);
            Date sliceEndTime = new Date(sliceStartTime.getTime() + timeSlice);
            SliceCamTrajectoryCompare timeSliceObj = new SliceCamTrajectoryCompare(sliceStartTime, sliceEndTime);
            dividedLists.add(timeSliceObj);
        }

        // 遍历每个轨迹点，将其分配到相应的时间片段列表
        for (CamTrajectory trajectory : camTrajectories) {
            Date photoTime = trajectory.getPhotoTime();

            // 计算轨迹点在时间片段中的索引
            int sliceIndex = (int) ((photoTime.getTime() - startTime.getTime()) / timeSlice);

            // 将轨迹点添加到相应的时间片段列表
            SliceCamTrajectoryCompare timeSliceObj = dividedLists.get(sliceIndex);
            timeSliceObj.getTrajectories().add(trajectory);
        }

        return dividedLists;
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
