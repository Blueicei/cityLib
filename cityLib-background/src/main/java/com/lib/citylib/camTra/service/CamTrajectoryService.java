package com.lib.citylib.camTra.service;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.Query.QueryCityFlowStats;
import com.lib.citylib.camTra.dto.*;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.Query.QueryVehicleAppearanceByCar;
import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.utils.GPSUtil;
import com.opencsv.CSVReader;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CamTrajectoryService {
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;

    public List<CarNumberAndCarTypeByCount> listAllCarNumberAndCarTypeByCount(){
        return camTrajectoryMapper.listAllCarNumberAndCarTypeByCount();
    }

    public List<CarNumberAndCarTypeByCount> listAllCarNumberAndCarTypeByCountString(String carNumber){
        return camTrajectoryMapper.listAllCarNumberAndCarTypeByCountString(carNumber);
    }

    public int allCarCount(){
        return camTrajectoryMapper.getAllCarCount();
    }

    public int allCamCount(){
        return camTrajectoryMapper.getAllCamCount();
    }

    public int localCarCount(){
        return camTrajectoryMapper.getLocalCarCount();
    }

    public int flow(){
        return camTrajectoryMapper.getFlow();
    }

    public List<FlowStats> getAllFlowStats(){
        return camTrajectoryMapper.getAllFlowStats();
    }

    public List<FlowStats> getLocalFlowStats(){
        return camTrajectoryMapper.getLocalFlowStats();
    }

    public List<FlowStats> getForeignFlowStats(){
        return camTrajectoryMapper.getForeignFlowStats();
    }


    public List<String> getAllCarNumber(){
        return camTrajectoryMapper.getAllCarNumber();
    }

    public Map<String, Integer> highestFlowTime(Date startTime, Date endTime) throws ParseException {
        Map<String, Integer> camTrajectories = camTrajectoryMapper.getAllCamTrajectory(startTime,endTime);
//        System.out.println(camTrajectories);
//        String dateString = "2021-02-01 00:00:00";
//        String dateString1 = "2021-02-01 23:59:59";
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date startTime = format.parse(dateString);
//        Date endTime = format.parse(dateString1);
//        List<SliceCamTrajectoryCompare> dividedLists = splitCompare(camTrajectories, startTime, endTime, 60);
//        SliceCamTrajectoryCompare sliceCamTrajectoryCompare = new SliceCamTrajectoryCompare();
//        int count = 0;
//        for (SliceCamTrajectoryCompare timeSlice : dividedLists) {
//            if (timeSlice.getTrajectories().size() > count)
//                sliceCamTrajectoryCompare = timeSlice;
//        }
        return camTrajectories;
    }

    public List<HotMap> getHotMapInfoByTime(StartToEndTime startToEndTime) {

        List<CamInfoCount> list = camTrajectoryMapper.searchCamInfoCount(startToEndTime.getStartTime(),startToEndTime.getEndTime());
        int max = 0;
        for(CamInfoCount count:list){
            if(count.getCount()>max){
                max = count.getCount();
            }
        }
        List<HotMap> hotMaps = new ArrayList<>();
        for(CamInfoCount count:list){
            double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
            int c_1 = (int)c;
            double lon = count.getCamLon();
            double lat = count.getCamLat();
            double[] points = new double[2];
            points = GPSUtil.gps84_To_Gcj02(lat, lon);
            HotMap hotMap = new HotMap(points[1],points[0], c_1);
            hotMaps.add(hotMap);
        }

        return hotMaps;
    }

    public List<HotMap> getForeignHotMapInfoByTime(StartToEndTime startToEndTime) {

        List<CamInfoCount> list = camTrajectoryMapper.searchForeignCamInfoCount(startToEndTime.getStartTime(),startToEndTime.getEndTime());
        int max = 0;
        for(CamInfoCount count:list){
            if(count.getCount()>max){
                max = count.getCount();
            }
        }
        List<HotMap> hotMaps = new ArrayList<>();
        for(CamInfoCount count:list){
            double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
            int c_1 = (int)c;
            double lon = count.getCamLon();
            double lat = count.getCamLat();
            double[] points = new double[2];
            points = GPSUtil.gps84_To_Gcj02(lat, lon);
            HotMap hotMap = new HotMap(points[1],points[0], c_1);
            hotMaps.add(hotMap);
        }

        return hotMaps;
    }

    public List<HotMap> getLocalHotMapInfoByTime(StartToEndTime startToEndTime) {

        List<CamInfoCount> list = camTrajectoryMapper.searchLocalCamInfoCount(startToEndTime.getStartTime(),startToEndTime.getEndTime());
        int max = 0;
        for(CamInfoCount count:list){
            if(count.getCount()>max){
                max = count.getCount();
            }
        }
        List<HotMap> hotMaps = new ArrayList<>();
        for(CamInfoCount count:list){
            double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
            int c_1 = (int)c;
            double lon = count.getCamLon();
            double lat = count.getCamLat();
            double[] points = new double[2];
            points = GPSUtil.gps84_To_Gcj02(lat, lon);
            HotMap hotMap = new HotMap(points[1],points[0], c_1);
            hotMaps.add(hotMap);
        }

        return hotMaps;
    }

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
//                        flatMap(new CutPointsToTrajectory(30)).
//                        filter((List<CamTrajectory> l1) -> {
//                            return l1.size() >= 2;
//                        }).
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
                    filter((List<CamTrajectory> l1) -> {
                        return l1.size() > 3;
                    }).
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
            if (i == 0) {
                List<String> carNumbers = camTrajectoryMapper.vehicleCountByCam(allcamId.get(i), vehicleCountByCamDto.getStartTime(), vehicleCountByCamDto.getEndTime());
//                System.out.println(carNumbers);
                Map<String, Integer> occurrences = new HashMap<>();
                for (String element : carNumbers) {
                    occurrences.put(element, occurrences.getOrDefault(element, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
                    String element = entry.getKey();
                    int count = entry.getValue();
                    queryVehicleCountByCams.add(new QueryVehicleCountByCam(element, allcamId.get(i), count));
                }
            } else {
                List<String> carNumbers = camTrajectoryMapper.vehicleCountByCam(allcamId.get(i), vehicleCountByCamDto.getStartTime(), vehicleCountByCamDto.getEndTime());
                Map<String, Integer> occurrences = new HashMap<>();
                for (String element : carNumbers) {
                    occurrences.put(element, occurrences.getOrDefault(element, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
                    String element = entry.getKey();
                    int count = entry.getValue();
                    int flag = 0;
                    for (int j = 0; j < queryVehicleCountByCams.size(); j++) {
                        if (queryVehicleCountByCams.get(j).getCarNumber().equals(element)) {
                            queryVehicleCountByCams.get(j).addCount(count);
                            queryVehicleCountByCams.get(j).addCam(allcamId.get(i));
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        queryVehicleCountByCams.add(new QueryVehicleCountByCam(element, allcamId.get(i), count));
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
            queryVehicleAppearanceByCars.add(new QueryVehicleAppearanceByCar(allCarNumbers.get(i), count));
        }
        return queryVehicleAppearanceByCars;
    }


    /**
     * 6.25
     *
     * @param camCountByCarDto
     * @return List<QueryCamCountByCar>
     * @throws Exception
     */


    public List<QueryCamCountByCar> listCamCountByCar(CamCountByCarDto camCountByCarDto) {
        String carNumber = camCountByCarDto.getCarNumber();
        List<QueryCamCountByCar> list = camTrajectoryMapper.listCamCountByCar(carNumber, camCountByCarDto.getStartTime(), camCountByCarDto.getEndTime());
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
                        filter((List<CamTrajectory> l1) -> {
                            return l1.size() > 3;
                        }).
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

    public List<QueryCityFlowStats> cityFlowStats(VehicleCountByCamDto vehicleCountByCamDto) {
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

    public List<CompareVehicleStats> compareVehiclesStats(ForeignVehicleStatsDto foreignVehicleStatsDto) {
        List<String> camids = foreignVehicleStatsDto.getCamIds();
        Date startTime = foreignVehicleStatsDto.getStartTime();
        Date endTime = foreignVehicleStatsDto.getEndTime();
        List<CompareVehicleStats> compareVehicleStatsList = new ArrayList<>();
        int granularity = foreignVehicleStatsDto.getGranularity();
        for (int i = 0; i < camids.size(); i++) {

            List<CamTrajectory> camTrajectories = camTrajectoryMapper.compareVehiclesStats(camids.get(i), startTime, endTime);

            List<SliceCamTrajectoryCompare> dividedLists = splitCompare(camTrajectories, startTime, endTime, granularity);
            for (SliceCamTrajectoryCompare timeSlice : dividedLists) {
                timeSlice.setCount(timeSlice.getTrajectories().size());
            }
            compareVehicleStatsList.add(new CompareVehicleStats(camids.get(i), dividedLists));
            System.out.println(new CompareVehicleStats(camids.get(i), dividedLists));
        }

        return compareVehicleStatsList;

    }

    public List<ForeignVehicleStats> foreignVehiclesStats(ForeignVehicleStatsDto foreignVehicleStatsDto) {
        List<String> camids = foreignVehicleStatsDto.getCamIds();
        Date startTime = foreignVehicleStatsDto.getStartTime();
        Date endTime = foreignVehicleStatsDto.getEndTime();
        List<ForeignVehicleStats> foreignVehicleStatsList = new ArrayList<>();
        int granularity = foreignVehicleStatsDto.getGranularity();
        for (int i = 0; i < camids.size(); i++) {

            List<CamTrajectory> camTrajectories = camTrajectoryMapper.foreignVehiclesStats(camids.get(i), startTime, endTime);

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
            foreignVehicleStatsList.add(new ForeignVehicleStats(camids.get(i), dividedLists));
        }

        return foreignVehicleStatsList;

    }

    public List<CarTrajectory> multiRegionAnalysis(RegionDto regionDto) throws Exception {
        List<CamTrajectory> camTrajectories = camTrajectoryMapper.multiRegionAnalysis(
                regionDto.getLeft(), regionDto.getRight(),
                regionDto.getUp(), regionDto.getDown(),
                regionDto.getStartTime(), regionDto.getEndTime());

        List<CarTrajectory> carTrajectoryList = new ArrayList<>();
        if (camTrajectories.size() > 0) {
            Map<String, List<CamTrajectory>> groupedMap = camTrajectories.stream()
                    .collect(Collectors.groupingBy(CamTrajectory::getCarNumber));

            for (Map.Entry<String, List<CamTrajectory>> entry : groupedMap.entrySet()) {
                String carNumber = entry.getKey();
                List<CamTrajectory> trajectoryList = entry.getValue();
                if (trajectoryList.size() > 6){
                    String carType = trajectoryList.get(0).getCarType(); // 假设所有车辆的类型一致
                    CarTrajectory carTrajectory = new CarTrajectory(carNumber, carType, trajectoryList);
                    carTrajectoryList.add(carTrajectory);
                }
            }
        }

        return carTrajectoryList;

    }

    public List<CarTrajectoryWithTerminal> regionDestinationAnalysis(RegionDto regionDto) throws Exception {
        List<CamTrajectory> camTrajectories = camTrajectoryMapper.multiRegionAnalysis(
                regionDto.getLeft(), regionDto.getRight(),
                regionDto.getUp(), regionDto.getDown(),
                regionDto.getStartTime(), regionDto.getEndTime());

        List<CarTrajectoryWithTerminal> carTrajectoryWithTerminals = new ArrayList<>();
        if (camTrajectories.size() > 0) {
            Map<String, List<CamTrajectory>> groupedMap = camTrajectories.stream()
                    .collect(Collectors.groupingBy(CamTrajectory::getCarNumber));

            for (Map.Entry<String, List<CamTrajectory>> entry : groupedMap.entrySet()) {
                String carNumber = entry.getKey();
                List<CamTrajectory> camTrajectoryList = entry.getValue();
                if (camTrajectoryList.size() > 6){
                    int size = camTrajectoryList.size();
                    String carType = camTrajectoryList.get(0).getCarType(); // 假设所有车辆的类型一致
                    CarTrajectory carTrajectory = new CarTrajectory(carNumber, carType, camTrajectoryList);
                    CarTrajectoryWithTerminal carTrajectoryWithTerminal = new CarTrajectoryWithTerminal(carTrajectory,
                            new Point(camTrajectoryList.get(0).getCamLon(),camTrajectoryList.get(0).getCamLat()),
                            new Point(camTrajectoryList.get(size-1).getCamLon(),camTrajectoryList.get(size-1).getCamLat()),carNumber);

                    carTrajectoryWithTerminals.add(carTrajectoryWithTerminal);
                }
            }
        }

        return carTrajectoryWithTerminals;

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


    public List<String> getAllcarTypes() {
        List<String> carTypes = camTrajectoryMapper.getAllCarTypes();
        return carTypes;
    }

//    public List<CarTrajectoryWithTerminal> carTrajectoryAnalysis(CarTrajectoryAnalysisDto carTrajectoryAnalysis) {
//        String dateString = "Thu Jan 01 08:00:00 CST 1970";
//        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
//        try {
//            Date date1 = format.parse(dateString);
//            System.out.println(carTrajectoryAnalysis.getCutTime());
//            List<CarTrajectoryWithTerminal> list = new ArrayList<>();
//            Date currentDate = camTrajectoryMapper.findFirstTime(carTrajectoryAnalysis.getCarNumber(),carTrajectoryAnalysis.getStartTime(),carTrajectoryAnalysis.getEndTime());
//            if (currentDate.equals(date1)){
//                return list;
//            }
//            while((currentDate.compareTo(carTrajectoryAnalysis.getEndTime())) < 0){
//                System.out.println("currentTime"+currentDate);
//                Calendar rightNow = Calendar.getInstance();
//                rightNow.setTime(currentDate);
//                rightNow.add(Calendar.MINUTE,carTrajectoryAnalysis.getCutTime());
//                Date endTime = rightNow.getTime();
//                System.out.println("endTime:"+endTime);
//                if(endTime.compareTo(carTrajectoryAnalysis.getEndTime())<0){
//                    List<CamTrajectory> camTrajectoryList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carTrajectoryAnalysis.getCarNumber(),currentDate,endTime);
//                    CarTrajectory carTrajectory = new CarTrajectory(carTrajectoryAnalysis.getCarNumber(),camTrajectoryList.get(0).getCarType(),camTrajectoryList);
//                    Point start = new Point(camTrajectoryList.get(0).getCamLon(),camTrajectoryList.get(0).getCamLat());
//                    Point end = new Point(camTrajectoryList.get(camTrajectoryList.size()-1).getCamLon(),camTrajectoryList.get(camTrajectoryList.size()-1).getCamLat());
//                    CarTrajectoryWithTerminal carTrajectoryWithTerminal = new CarTrajectoryWithTerminal(carTrajectory,start,end,carTrajectoryAnalysis.getCarNumber());
//
//                    list.add(carTrajectoryWithTerminal);
//
//                    currentDate = camTrajectoryMapper.findFirstTime(carTrajectoryAnalysis.getCarNumber(),endTime,carTrajectoryAnalysis.getEndTime());
//                    if (currentDate.equals(date1)){
//                        return list;
//                    }
//                }else {
//                    List<CamTrajectory> camTrajectoryList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carTrajectoryAnalysis.getCarNumber(),currentDate,carTrajectoryAnalysis.getEndTime());
//                    CarTrajectory carTrajectory = new CarTrajectory(carTrajectoryAnalysis.getCarNumber(),camTrajectoryList.get(0).getCarType(),camTrajectoryList);
//                    Point start = new Point(camTrajectoryList.get(0).getCamLon(),camTrajectoryList.get(0).getCamLat());
//                    Point end = new Point(camTrajectoryList.get(camTrajectoryList.size()-1).getCamLon(),camTrajectoryList.get(camTrajectoryList.size()-1).getCamLat());
//                    CarTrajectoryWithTerminal carTrajectoryWithTerminal = new CarTrajectoryWithTerminal(carTrajectory,start,end,carTrajectoryAnalysis.getCarNumber());
//                    list.add(carTrajectoryWithTerminal);
//
//                    return list;
//                }
//            }
//            return list;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    public List<CarTrajectoryWithTerminal> carTrajectoryAnalysis(CarTrajectoryAnalysisDto carTrajectoryAnalysis) throws Exception {
        List<CarTrajectoryWithTerminal> carTrajectoryWithTerminals = new ArrayList<>();
        List<CamTrajectory> camTrajectoryList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carTrajectoryAnalysis.getCarNumber(), carTrajectoryAnalysis.getStartTime(), carTrajectoryAnalysis.getEndTime());
        if (camTrajectoryList.size() > 0) {
            String carType = camTrajectoryList.get(0).getCarType();
            String carNumber = camTrajectoryList.get(0).getCarNumber();
            CarTrajectory carTrajectory = new CarTrajectory(carNumber, carType, camTrajectoryList);
//                    System.out.println(carTrajectory.getPoints().get(0).getCamId());
//                    System.out.println(carTrajectory.getPoints());

            ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
            DataSet<CamTrajectory> points = env.fromCollection(carTrajectory.getPoints()).name("row-camtra-points");

            //carTrajectories.add(carTrajectory);
            DataSet<CarTrajectory> newPoints = points.
                    filter(new LonLatNotNullFilter()).
                    sortPartition(CamTrajectory::getPhotoTime, Order.ASCENDING).
                    map(new PointListMap()).
                    reduce(new MergePoints()).
                    flatMap(new CutPointsToTrajectory(carTrajectoryAnalysis.getCutTime())).
//                    filter((List<CamTrajectory> l1) -> {
//                        return l1.size() > 2;
//                    }).
                    map(new PointListToTraMap()).
//                    filter((CarTrajectory c) -> {
//                        for (CamTrajectory point : c.getPoints()) {
//                            if (trajectoryDto.getCamIds().contains(point.getCamId())) {
//                                return true;
//                            }
//                        }
//                        return false;
//                    }).
                    name("points-to-trajectory");

            List<CarTrajectory> newTraList = newPoints.collect();
            for(CarTrajectory carTra:newTraList){
                for(CamTrajectory camTrajectory:carTra.getPoints()){
                    double[] p = new double[2];
                    p = GPSUtil.gps84_To_Gcj02(camTrajectory.getCamLat(), camTrajectory.getCamLon());
                    camTrajectory.setCamLat(p[0]);
                    camTrajectory.setCamLat(p[1]);
                }
                CarTrajectoryWithTerminal carTrajectoryWithTerminal = new CarTrajectoryWithTerminal(carTra,carTrajectoryAnalysis.getCarNumber());
                carTrajectoryWithTerminals.add(carTrajectoryWithTerminal);
            }
        }
        return carTrajectoryWithTerminals;
    }

    public List<HotMap> getHotMap(CarTrajectory carTrajectory) {
        Map<String, Integer> camCount = new HashMap<>();
        for (CamTrajectory camTrajectory : carTrajectory.getPoints()) {
            String camId = camTrajectory.getCamId();
            if (camCount.containsKey(camId)) {
                camCount.put(camId, camCount.get(camId) + 1);
            } else {
                camCount.put(camId, 1);
            }
        }
        int max = 0;
        for (Map.Entry<String, Integer> entry : camCount.entrySet()) {
            if(entry.getValue()>max){
                max = entry.getValue();
            }
        }
        List<HotMap> hotMaps = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : camCount.entrySet()) {
            int count = (int)(100*Math.log(entry.getValue()+1) / Math.log(max+1));
            Point point = camTrajectoryMapper.getPoint(entry.getKey());
            double[] points = new double[2];
            points = GPSUtil.gps84_To_Gcj02(point.getLat(), point.getLon());
            point.setLat(points[0]);
            point.setLon(points[1]);
            HotMap hotMap = new HotMap(point, count);
            hotMaps.add(hotMap);
        }
        return hotMaps;
    }

//    public List<List<HotMap>> getHotMapByCutTime(StartToEndTimeWithTimeCut startToEndTime) {
//        Date currentDate = startToEndTime.getStartTime();
//        Date endDate = startToEndTime.getEndTime();
//        List<List<HotMap>> lists = new ArrayList<>();
//        while(currentDate.compareTo(endDate)<0){
//            Calendar rightNow = Calendar.getInstance();
//            rightNow.setTime(currentDate);
//            rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
//            Date nextDate = rightNow.getTime();
//            if(nextDate.compareTo(endDate)<0){
//            StartToEndTime startToEndTime1 = new StartToEndTime(currentDate,nextDate);
//            List<HotMap> list = this.getHotMapInfoByTime(startToEndTime1);
//            lists.add(list);
//            }else {
//                StartToEndTime startToEndTime1 = new StartToEndTime(currentDate,endDate);
//                List<HotMap> list = this.getHotMapInfoByTime(startToEndTime1);
//                lists.add(list);
//                return lists;
//            }
//            currentDate = nextDate;
//        }
//        return lists;
//    }

    public List<List<HotMap>> getHotMapByCutTime(StartToEndTimeWithTimeCut startToEndTime) {
        Date currentDate = startToEndTime.getStartTime();
        Date endDate = startToEndTime.getEndTime();
        List<List<CamInfoCount>> camInfoCountList = new ArrayList<>();
        while(currentDate.compareTo(endDate)<0){
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(currentDate);
            rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
            Date nextDate = rightNow.getTime();
            if(nextDate.compareTo(endDate)<0){
                StartToEndTime startToEndTime1 = new StartToEndTime(currentDate,nextDate);
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchCamInfoCount(currentDate,nextDate);
                camInfoCountList.add(camInfoCounts);
            }else {
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchCamInfoCount(currentDate,endDate);
                camInfoCountList.add(camInfoCounts);
            }
            currentDate = nextDate;
        }
        int max = 0;
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            for(CamInfoCount count:camInfoCounts){
                if(count.getCount()>=max){
                    max = count.getCount();
                }
            }
        }
        List<List<HotMap>> lists = new ArrayList<>();
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            List<HotMap> hotMaps = new ArrayList<>();
            for(CamInfoCount count:camInfoCounts){
                double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
                int c_1 = (int)c;
                double lon = count.getCamLon();
                double lat = count.getCamLat();
                double[] points = new double[2];
                points = GPSUtil.gps84_To_Gcj02(lat, lon);
                HotMap hotMap = new HotMap(points[1],points[0], c_1);
                hotMaps.add(hotMap);
            }
            lists.add(hotMaps);
        }

        return lists;
    }

    public List<List<HotMap>> getLocalHotMapByCutTime(StartToEndTimeWithTimeCut startToEndTime) {
        Date currentDate = startToEndTime.getStartTime();
        Date endDate = startToEndTime.getEndTime();
        List<List<CamInfoCount>> camInfoCountList = new ArrayList<>();
        while(currentDate.compareTo(endDate)<0){
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(currentDate);
            rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
            Date nextDate = rightNow.getTime();
            if(nextDate.compareTo(endDate)<0){
                StartToEndTime startToEndTime1 = new StartToEndTime(currentDate,nextDate);
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchLocalCamInfoCount(currentDate,nextDate);
                camInfoCountList.add(camInfoCounts);
            }else {
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchLocalCamInfoCount(currentDate,endDate);
                camInfoCountList.add(camInfoCounts);
            }
            currentDate = nextDate;
        }
        int max = 0;
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            for(CamInfoCount count:camInfoCounts){
                if(count.getCount()>=max){
                    max = count.getCount();
                }
            }
        }
        List<List<HotMap>> lists = new ArrayList<>();
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            List<HotMap> hotMaps = new ArrayList<>();
            for(CamInfoCount count:camInfoCounts){
                double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
                int c_1 = (int)c;
                double lon = count.getCamLon();
                double lat = count.getCamLat();
                double[] points = new double[2];
                points = GPSUtil.gps84_To_Gcj02(lat, lon);
                HotMap hotMap = new HotMap(points[1],points[0], c_1);
                hotMaps.add(hotMap);
            }
            lists.add(hotMaps);
        }

        return lists;
    }

    public List<List<HotMap>> getForeignHotMapByCutTime(StartToEndTimeWithTimeCut startToEndTime) {
        Date currentDate = startToEndTime.getStartTime();
        Date endDate = startToEndTime.getEndTime();
        List<List<CamInfoCount>> camInfoCountList = new ArrayList<>();
        while(currentDate.compareTo(endDate)<0){
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(currentDate);
            rightNow.add(Calendar.HOUR, startToEndTime.getCutTime());
            Date nextDate = rightNow.getTime();
            if(nextDate.compareTo(endDate)<0){
                StartToEndTime startToEndTime1 = new StartToEndTime(currentDate,nextDate);
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchForeignCamInfoCount(currentDate,nextDate);
                camInfoCountList.add(camInfoCounts);
            }else {
                List<CamInfoCount> camInfoCounts = camTrajectoryMapper.searchForeignCamInfoCount(currentDate,endDate);
                camInfoCountList.add(camInfoCounts);
            }
            currentDate = nextDate;
        }
        int max = 0;
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            for(CamInfoCount count:camInfoCounts){
                if(count.getCount()>=max){
                    max = count.getCount();
                }
            }
        }
        List<List<HotMap>> lists = new ArrayList<>();
        for(List<CamInfoCount> camInfoCounts:camInfoCountList){
            List<HotMap> hotMaps = new ArrayList<>();
            for(CamInfoCount count:camInfoCounts){
                double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
                int c_1 = (int)c;
                double lon = count.getCamLon();
                double lat = count.getCamLat();
                double[] points = new double[2];
                points = GPSUtil.gps84_To_Gcj02(lat, lon);
                HotMap hotMap = new HotMap(points[1],points[0], c_1);
                hotMaps.add(hotMap);
            }
            lists.add(hotMaps);
        }

        return lists;
    }

    public List<HotMap> getHeatMapByCarNumber(CarWithTime carWithTime) {
        List<String> list = new ArrayList<>();
        list.add(carWithTime.getCarNumber());
        List<CamInfoCount> camInfoCounts = camTrajectoryMapper.getHeatMapByCarNumber(list, carWithTime.getStartTime(), carWithTime.getEndTime());

            int max = 0;
            for (CamInfoCount count : camInfoCounts) {
                if (count.getCount() > max) {
                    max = count.getCount();
                }
            }

        List<HotMap> hotMaps = new ArrayList<>();
        for(CamInfoCount count:camInfoCounts){
            double c = 100.0*Math.log(count.getCount()+1) / Math.log(max+1);
            int c_1 = (int)c;
            double lon = count.getCamLon();
            double lat = count.getCamLat();
            double[] points = new double[2];
            points = GPSUtil.gps84_To_Gcj02(lat, lon);
            HotMap hotMap = new HotMap(points[1],points[0], c_1);
            hotMaps.add(hotMap);
        }

        return hotMaps;
    }

    public List<POI> POISearch(String carNumber) {
        String date1 = "2021-02-01 00:00:00";
        String date2 = "2021-02-02 00:00:00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> list = new ArrayList<>();
        list.add(carNumber);
        try {
            // 将日期字符串解析为Date对象
            Date parsedDate1 = dateFormat.parse(date1);
            Date parsedDate2 = dateFormat.parse(date2);
            // 输出解析后的Date对象
            HashMap<String,Integer> map1 = new HashMap<>();
            HashMap<String,Integer> map2 = new HashMap<>();
            CamLocation camLocation = new CamLocation();
            List<CarTrajectory> carTrajectories = this.listByCarNumberOrderInTimeRange(list,parsedDate1,parsedDate2);
            for(CarTrajectory carTrajectory:carTrajectories){
//                for (CamTrajectory camTrajectory : carTrajectory.getPoints()) {
//                    String camId = camTrajectory.getCamId();
//                    if (camCount.containsKey(camId)) {
//                        camCount.put(camId, camCount.get(camId) + 1);
//                    } else {
//                        camCount.put(camId, 1);
//                    }
//                }
                List<CamTrajectory> camTrajectoryList = carTrajectory.getPoints();
                String camId1 = camTrajectoryList.get(0).getCamId();
                if(map1.containsKey(camId1)){
                    map1.put(camId1, map1.get(camId1) + 1);
                }else {
                    map1.put(camId1,  1);
                    camLocation.addToHashMap(camId1,new Point(camTrajectoryList.get(0).getCamLon(),camTrajectoryList.get(0).getCamLat()));
                }
                String camId2 = camTrajectoryList.get(camTrajectoryList.size()-1).getCamId();
                if(map2.containsKey(camId2)){
                    map2.put(camId2, map2.get(camId2) + 1);
                }else {
                    map2.put(camId2,  1);
                    camLocation.addToHashMap(camId2,new Point(camTrajectoryList.get(camTrajectoryList.size()-1).getCamLon(),camTrajectoryList.get(camTrajectoryList.size()-1).getCamLat()));
                }
            }
            List<POI>  pois = new ArrayList<>();
            List<Map.Entry<String, Integer>> list1 = new ArrayList<>(map1.entrySet());

            // Sort the list in descending order based on the value using Comparator
            Collections.sort(list1, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            List<Map.Entry<String, Integer>> list2 = new ArrayList<>(map2.entrySet());

            // Sort the list in descending order based on the value using Comparator
            Collections.sort(list2, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            int count = 0;
            for(Map.Entry<String, Integer> entry : list1){
                String camId = entry.getKey();
                POI poi = new POI(camLocation.getValueFromHashMap(camId),0);
                pois.add(poi);
                count++;
                if(count>=3){
                    break;
                }
            }
            count = 0;
            for(Map.Entry<String, Integer> entry : list2){
                String camId = entry.getKey();
                POI poi = new POI(camLocation.getValueFromHashMap(camId),1);
                pois.add(poi);
                count++;
                if(count>=3){
                    break;
                }
            }
            System.out.println("------------------");
            return pois;
        } catch (ParseException e) {
            // 解析失败时的异常处理
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<POI>();
    }

    public List<GirdFlow> gridFlowCount(Gird gird) {
        double[] leftup = GPSUtil.gcj02_To_Gps84(gird.getUp(), gird.getLeft());
        double left = leftup[1];
        double up = leftup[0];

        double[] rightdown = GPSUtil.gcj02_To_Gps84(gird.getDown(), gird.getRight());
        double right = rightdown[1];
        double down = rightdown[0];

        List<GirdFlow> list = new ArrayList<>();
        for(int i=0;i<gird.getCut();i++){
            for(int j=0;j<gird.getCut();j++){
                GirdFlow gridFlow = new GirdFlow();
                gridFlow.setGirdId("网格"+(i+1)+"-"+(j+1));
                gridFlow.setRow(i+1);
                gridFlow.setCol(j+1);
                double col = right-left;
                double row = up-down;
                double gridRow = row/4.0;
                double gridcol = col/4.0;
                double gridLeft = left + j * gridcol;
                double gridRight = gridLeft+ gridcol;
                double gridUp = up - i * gridRow;
                double gridDown = gridUp -gridRow;
                gridFlow.setCount(camTrajectoryMapper.getGridFlow(gridLeft,gridRight,gridUp,gridDown,gird.getStartTime(),gird.getEndTime()));
                double[] gcjCentralPoint = GPSUtil.gps84_To_Gcj02((gridUp+gridDown)/2.0, (gridLeft+gridRight)/2.0);
                double[] leftups = GPSUtil.gps84_To_Gcj02(gridUp,gridLeft);
                double[] rightdowns = GPSUtil.gps84_To_Gcj02(gridDown,gridRight);
                gridFlow.setLeft(leftups[1]);
                gridFlow.setUp(leftups[0]);
                gridFlow.setRight(rightdowns[1]);
                gridFlow.setDown(rightdowns[0]);
                gridFlow.setCentralLon(gcjCentralPoint[1]);
                gridFlow.setCentralLat(gcjCentralPoint[0]);
                gridFlow.setPoi("");
                list.add(gridFlow);
            }
        }
        Collections.sort(list, Comparator.comparingInt(GirdFlow::getCount).reversed());
//        return list.subList(0,100);
        return list;
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
            for (int i = 1; i < camTrajectories.size(); i++) {
                CamTrajectory afterPoint = camTrajectories.get(i);
//                System.out.println(afterPoint.getPhotoTime().getTime());
//                System.out.println(beforePoint.getPhotoTime().getTime());
//                System.out.println(afterPoint.getPhotoTime().getTime() - beforePoint.getPhotoTime().getTime());
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