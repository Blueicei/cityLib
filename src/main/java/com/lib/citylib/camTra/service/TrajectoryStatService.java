package com.lib.citylib.camTra.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lib.citylib.camTra.dto.ClusterFlowDto;
import com.lib.citylib.camTra.dto.ClusterInfoDto;
import com.lib.citylib.camTra.dto.StartToEndTime;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.CarTrajectoryPlus;
import com.lib.citylib.camTra.model.ClusterFlowInfo;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.query.QueryClusterFlow;
import com.lib.citylib.camTra.query.QueryTableStat;
import com.lib.citylib.camTra.utils.ReplaceTableInterceptor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrajectoryStatService {
    @Resource
    private TrajectoryStatMapper trajectoryStatMapper;
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private ReplaceTableInterceptor replaceTableInterceptor;

    public HashMap<String,Object> getTotalStat(ListStatisticsParam param){
        return trajectoryStatMapper.getTotalStat(param);
    }

    public HashMap<String, Map<Object,Long>> getStatByCar(ListStatisticsParam param) throws Exception {
        List<CarTrajectory> trajectories = trajectoryStatMapper.getStatByCar(param);

        Map<Object,Long> traTimeDistribute = new HashMap<>();
        traTimeDistribute.put("0-30min", trajectories.
                stream()
                .filter(e -> (e.getTimeInterval() < 30 * 60) ).count());
        traTimeDistribute.put("30min-1h", trajectories.
                stream()
                .filter(e -> (e.getTimeInterval() >= 30 * 60 && e.getTimeInterval() < 3600) ).count());
        traTimeDistribute.put("1-2h", trajectories.
                stream()
                .filter(e -> (e.getTimeInterval() >= 3600 && e.getTimeInterval() < 3600 * 2) ).count());
        traTimeDistribute.put("2h+", trajectories.
                stream()
                .filter(e -> (e.getTimeInterval() >= 3600 * 2) ).count());

        Map<Object, Long> traDistanceDistribute =new HashMap<>();
        for(int i = 0; i < 10; i ++){
            String key = i + "-" + (i + 1) + "km";
            int finalI = i;
            traDistanceDistribute.put(key, trajectories
                    .stream()
                    .filter(e -> (e.getDistance() >= 1000 * finalI && e.getDistance() < 1000 * (finalI + 1)))
                    .count());
        }
        for(int i = 10; i < 100; i += 10){
            String key = i + "-" + (i + 10) + "km";
            int finalI = i;
            traDistanceDistribute.put(key, trajectories
                    .stream()
                    .filter(e -> (e.getDistance() >= 1000 * finalI && e.getDistance() < 1000 * (finalI + 10)))
                    .count());
        }
        traDistanceDistribute.put("100km+", trajectories
                .stream()
                .filter(e -> (e.getDistance() >= 1000 * 100))
                .count());

        Map<Object, Long> traStartTimePerHour = new HashMap<>();
        for (int i = 0; i < 24; i++){
            int finalI = i;
            traStartTimePerHour.put(i,trajectories
                    .stream()
                    .filter(e -> {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(e.getStartTime());
                        return calendar.get(Calendar.HOUR_OF_DAY) == finalI;
                    }).count());
        }
        Map<Object, Long> traStartTimePerDay = new HashMap<>();
        for (int i = 1; i < 32; i++){
            int finalI = i;
            traStartTimePerDay.put(i,trajectories
                    .stream()
                    .filter(e -> {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(e.getStartTime());
                        return calendar.get(Calendar.DAY_OF_MONTH) == finalI;
                    }).count());
        }

        HashMap<String, Map<Object,Long>> res = new HashMap<>();
        res.put("traTimeDistribute",traTimeDistribute);
        res.put("traDistanceDistribute",traDistanceDistribute);
        res.put("traStartTimePerHour",traStartTimePerHour);
        res.put("traStartTimePerDay",traStartTimePerDay);
        return res;
    }
    public IPage<TrajectoryStat> listWithPage(ListStatisticsParam param) throws Exception {
        if(param.getPageNum() == null || param.getPageSize() == null){
            throw new Exception("分页信息不完整");
        }

        Page<TrajectoryStat> page = new Page<>(param.getPageNum(),param.getPageSize());
        page.addOrder(param.getIsDesc()? OrderItem.desc(param.getOrderBy()) : OrderItem.asc(param.getOrderBy()));
        IPage<TrajectoryStat> res = trajectoryStatMapper.listStatByAll(page,param);

        return res;
    }

    public List<List<QueryClusterFlow>> getClusterFlow(ClusterFlowDto clusterFlowDto){
        List<ClusterInfoDto> allCluster = clusterFlowDto.getAllCluster();
        HashMap<String, String> camToClusterMap = new HashMap<>();
        for(ClusterInfoDto c:allCluster){
            String clusterName = c.getClusterName();
            for(String point:c.getPoints()){
                camToClusterMap.put(point, clusterName);
            }
        }
        List<ClusterInfoDto> targetCluster = clusterFlowDto.getTargetCluster();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = clusterFlowDto.getSearchDate();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 1);
        Date secondDate = calendar.getTime();
        List<List<QueryClusterFlow>> newList = new ArrayList<>();
        while(currentDate.compareTo(secondDate)<0){
            List<QueryClusterFlow> queryClusterFlowList = new ArrayList<>();
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(currentDate);
            rightNow.add(Calendar.HOUR, clusterFlowDto.getCutTime());
            Date nextDate = rightNow.getTime();
            for(ClusterInfoDto target : targetCluster){
                HashMap<String, Integer> clusterCountMap = new HashMap<>();
                List<ClusterFlowInfo> resultFlow;
                if(target.getAsOrigin()){
                    resultFlow = trajectoryStatMapper.getFlowStatByStartCluster(target.getPoints(), currentDate, nextDate);
                }
                else {
                    resultFlow = trajectoryStatMapper.getFlowStatByEndCluster(target.getPoints(), currentDate, nextDate);
                }
                for(ClusterFlowInfo flow:resultFlow){
                    String clusterName = camToClusterMap.get(flow.getCamId());
                    int flowCount = flow.getCount();
                    if(clusterCountMap.containsKey(clusterName)){
                        clusterCountMap.put(clusterName, clusterCountMap.get(clusterName) + flowCount);
                    }
                    else {
                        clusterCountMap.put(clusterName, flowCount);
                    }
                }
                for(String clusterKey : clusterCountMap.keySet()){
                    if (clusterCountMap.get(clusterKey)>100){
                        QueryClusterFlow queryClusterFlow = new QueryClusterFlow();
                        if(target.getAsOrigin()){
                            queryClusterFlow.setTargetCluster(target.getClusterName());
                            queryClusterFlow.setArrowToCluster(clusterKey);
                        }
                        else {
                            queryClusterFlow.setTargetCluster(clusterKey);
                            queryClusterFlow.setArrowToCluster(target.getClusterName());
                        }
                        queryClusterFlow.setCount(clusterCountMap.get(clusterKey));
                        queryClusterFlowList.add(queryClusterFlow);
                    }
                }

            }
            newList.add(queryClusterFlowList);
            currentDate = nextDate;
        }
        return newList;
    }

    public QueryTableStat getTableStatByTime(StartToEndTime startToEndTime) {
        String tableName = replaceTableInterceptor.getTableName();
        List<CarTrajectoryPlus> list = trajectoryStatMapper.getTableStatByTime(tableName, startToEndTime.getStartTime(),startToEndTime.getEndTime());
        Long traCount = (long)list.size();
        Map<String, Long> carTypeTraCountMap = list.stream().collect(Collectors.groupingBy(CarTrajectoryPlus::getCarType, Collectors.counting()));
        Long carTypeCount = (long)carTypeTraCountMap.keySet().size();
        Long carNumberCount = list.stream().map(CarTrajectoryPlus::getCarNumber).distinct().count();
        Long pointNumberCount = list.stream().mapToLong(CarTrajectoryPlus::getPointNum).sum();

        Map<String, Long> dateTraCountMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = startToEndTime.getStartTime();
        Date endDate = startToEndTime.getEndTime();
        while(currentDate.compareTo(endDate)<0){
            calendar.setTime(currentDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endOfDay = calendar.getTime();

            if(endDate.compareTo(endOfDay)<=0){
                endOfDay = endDate;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
            String curDate = sdf.format(currentDate);  //当前日期
            Date finalCurrentDate = currentDate;
            Date finalEndOfDay = endOfDay;
            dateTraCountMap.put(curDate, list.stream().filter(e -> (finalCurrentDate.compareTo(e.getStartTime()) <= 0 && e.getStartTime().compareTo(finalEndOfDay) <= 0) ).count());

            calendar.setTime(currentDate);
            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            currentDate = calendar.getTime();
        }
        Map<Object,Long> traTimeDistribute = new HashMap<>();
        traTimeDistribute.put("0-30min", list.
                stream()
                .filter(e -> (e.getTimeInterval() < 30 * 60) ).count());
        traTimeDistribute.put("30min-1h", list.
                stream()
                .filter(e -> (e.getTimeInterval() >= 30 * 60 && e.getTimeInterval() < 3600) ).count());
        traTimeDistribute.put("1-2h", list.
                stream()
                .filter(e -> (e.getTimeInterval() >= 3600 && e.getTimeInterval() < 3600 * 2) ).count());
        traTimeDistribute.put("2h+", list.
                stream()
                .filter(e -> (e.getTimeInterval() >= 3600 * 2) ).count());

        Map<Object, Long> traDistanceDistribute =new HashMap<>();
        for(int i = 0; i < 10; i ++){
            String key = i + "-" + (i + 1) + "km";
            int finalI = i;
            traDistanceDistribute.put(key, list
                    .stream()
                    .filter(e -> (e.getDistance() >= 1000 * finalI && e.getDistance() < 1000 * (finalI + 1)))
                    .count());
        }
        for(int i = 10; i < 100; i += 10){
            String key = i + "-" + (i + 10) + "km";
            int finalI = i;
            traDistanceDistribute.put(key, list
                    .stream()
                    .filter(e -> (e.getDistance() >= 1000 * finalI && e.getDistance() < 1000 * (finalI + 10)))
                    .count());
        }
        traDistanceDistribute.put("100km+", list
                .stream()
                .filter(e -> (e.getDistance() >= 1000 * 100))
                .count());

        Map<Object, Long> traStartTimePerHour = new HashMap<>();
        for (int i = 0; i < 24; i++){
            int finalI = i;
            traStartTimePerHour.put(i,list
                    .stream()
                    .filter(e -> {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(e.getStartTime());
                        return cal.get(Calendar.HOUR_OF_DAY) == finalI;
                    }).count());
        }
        Map<Object, Long> traStartTimePerDay = new HashMap<>();
        for (int i = 1; i < 32; i++){
            int finalI = i;
            traStartTimePerDay.put(i,list
                    .stream()
                    .filter(e -> {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(e.getStartTime());
                        return cal.get(Calendar.DAY_OF_MONTH) == finalI;
                    }).count());
        }
        QueryTableStat queryTableStat = new QueryTableStat();
        queryTableStat.setTableName(tableName);
        queryTableStat.setQueryStartTime(startToEndTime.getStartTime());
        queryTableStat.setQueryEndTime(startToEndTime.getEndTime());
        queryTableStat.setTraCount(traCount);
        queryTableStat.setCarTypeCount(carTypeCount);
        queryTableStat.setCarNumberCount(carNumberCount);
        queryTableStat.setPointNumberCount(pointNumberCount);
        queryTableStat.setCarTypeTraCountMap(carTypeTraCountMap);
        queryTableStat.setDateTraCountMap(dateTraCountMap);
        queryTableStat.setTraTimeDistribute(traTimeDistribute);
        queryTableStat.setTraDistanceDistribute(traDistanceDistribute);
        queryTableStat.setTraStartTimePerHour(traStartTimePerHour);
        queryTableStat.setTraStartTimePerDay(traStartTimePerDay);
        return queryTableStat;
    }

}
