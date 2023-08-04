package com.lib.citylib.camTra.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lib.citylib.camTra.mapper.TaxiTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.ODResult;
import com.lib.citylib.camTra.model.taxi.Point;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.query.QueryODParam;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import javafx.beans.binding.ObjectExpression;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaxiTrajectoryService {
    @Resource
    private TaxiTrajectoryMapper taxiTrajectoryMapper;
    public HashMap<String,Object> getTotalStat(ListStatisticsParam param){
        return taxiTrajectoryMapper.getTotalStat(param);
    }

    public HashMap<String, Map<Object,Long>> getStatByCar(ListStatisticsParam param) throws Exception {
        List<TaxiTrajectory> trajectories = taxiTrajectoryMapper.getStatByCar(param);
        trajectories.forEach(e ->{
            e.setTimeInterval((int) DateUtil.between(e.getStartTime(),e.getEndTime(), DateUnit.SECOND));
        });

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
                    .filter(e -> (e.getDistanceCal() >= 1000 * finalI && e.getDistanceCal() < 1000 * (finalI + 1)))
                    .count());
        }
        for(int i = 10; i < 100; i += 10){
            String key = i + "-" + (i + 10) + "km";
            int finalI = i;
            traDistanceDistribute.put(key, trajectories
                    .stream()
                    .filter(e -> (e.getDistanceCal() >= 1000 * finalI && e.getDistanceCal() < 1000 * (finalI + 10)))
                    .count());
        }
        traDistanceDistribute.put("100km+", trajectories
                .stream()
                .filter(e -> (e.getDistanceCal() >= 1000 * 100))
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
        IPage<TrajectoryStat> res = taxiTrajectoryMapper.listStatByAll(page,param);

        return res;
    }

    public List<TaxiTrajectory> getTraByCar(ListStatisticsParam param) {
        return taxiTrajectoryMapper.getStatByCar(param);
    }

    public List<GpsPoint> getGpsPointByTra(String traId) {
        return taxiTrajectoryMapper.getGpsPointByTra(traId);
    }

    public List<GpsPoint> getGpsPointByCar(ListStatisticsParam param) {
        return taxiTrajectoryMapper.getGpsPointByCar(param);
    }

    public List<CamTrajectory> getCamPointByCar(ListStatisticsParam param) {
        return taxiTrajectoryMapper.getCamPointByCar(param);
    }
    @Value("${taxi.gps.folder}")
    private String gpsFolder;
    public IPage<HashMap<String,Object>> taxiList(int pageNum, int pageSize){
        IPage<HashMap<String,Object>> res = new Page<>();
        res.setSize(pageSize);
        res.setCurrent(pageNum);
        List<HashMap<String, Object>> list = new ArrayList<>();
        CsvReader reader = CsvUtil.getReader();
        if (pageNum < 1 || pageSize < 0){
            return res;
        }
        File folder = new File(gpsFolder);
//        File folder = new File("C:\\Users\\Zhang\\Desktop\\111");
        File[] carDirs = folder.listFiles();
        if (carDirs == null){
            return null;
        }
        res.setTotal(carDirs.length);
        if (pageSize * (pageNum - 1) > carDirs.length){
            return res;
        }

        for(int i = pageSize * (pageNum - 1); i < Math.min(pageNum * pageSize, carDirs.length); i ++){
            File carDir = carDirs[i];
            HashMap<String, Object> map = new HashMap<>();
            String carNumber = carDir.getName();
            int traCount = 0;
            File[] tripFiles = carDir.listFiles((dir, name) -> name.matches("trip.*"));
            for (File tripFile : tripFiles) {
                traCount += (reader.read(ResourceUtil.getUtf8Reader(tripFile.getAbsolutePath()), PartitionTraUtil.TrajectoryIn.class).size());
            }
            map.put("carNumber",carNumber);
            map.put("traCount",traCount);
            list.add(map);
        }
        res.setRecords(list);
        return res;
    }

    public List<Point> getGpsPoints(ListStatisticsParam param) throws Exception {
        if (DateUtil.between(DateUtil.parseDate(param.getMinTime()),DateUtil.parseDate(param.getMaxTime()),DateUnit.HOUR) > 24){
            throw new Exception("时间范围不能超过一天");
        }
        List<Point> res = new ArrayList<>();

        List<GpsPoint> gpsPoints = taxiTrajectoryMapper.getGpsPoints(param);
        Map<String,Integer> map = new HashMap<>();
        gpsPoints.forEach(e -> {
            String key = e.getLng() + "," + e.getLat();
            map.put(key, map.getOrDefault(key,0) + 1);
        });
        for (String key : map.keySet()) {
            Point point = new Point();
            point.setLng(Double.valueOf(key.split(",")[0]));
            point.setLat(Double.valueOf(key.split(",")[1]));
            point.setCount(map.get(key));
            res.add(point);
        }
        return res;
    }
    public List<List<Point>> getGpsPointsWithCut(ListStatisticsParam param) throws Exception {
        if (DateUtil.between(DateUtil.parseDate(param.getMinTime()),DateUtil.parseDate(param.getMaxTime()),DateUnit.HOUR) > 24){
            throw new Exception("时间范围不能超过一天");
        }
        List<List<Point>> res = new ArrayList<>();

        List<GpsPoint> gpsPoints = taxiTrajectoryMapper.getGpsPoints(param);

        List<List<GpsPoint>> tempList = new ArrayList<>();
        Date cur = DateUtil.parseDateTime(param.getMinTime());
        List<GpsPoint> tempPoint = new ArrayList<>();
        for (GpsPoint gpsPoint : gpsPoints) {
            if (DateUtil.compare(cur,gpsPoint.getTime()) <=0 && DateUtil.compare(DateUtil.offsetHour(cur,param.getCut()),gpsPoint.getTime()) > 0){
                tempPoint.add(gpsPoint);
            }else {
                tempList.add(tempPoint);
                cur = DateUtil.offsetHour(cur,param.getCut());
                tempPoint = new ArrayList<>();
            }
        }

        for (List<GpsPoint> points : tempList) {
            List<Point> temp = new ArrayList<>();
            Map<String,Integer> map = new HashMap<>();
            points.forEach(e -> {
                String key = e.getLng() + "," + e.getLat();
                map.put(key, map.getOrDefault(key,0) + 1);
            });
            for (String key : map.keySet()) {
                Point point = new Point();
                point.setLng(Double.valueOf(key.split(",")[0]));
                point.setLat(Double.valueOf(key.split(",")[1]));
                point.setCount(map.get(key));
                temp.add(point);
            }
            res.add(temp);
        }
        return res;
    }

    public ODResult getODsByCluster(QueryODParam param) {
        ODResult res = new ODResult();
        res.setFrom(taxiTrajectoryMapper.getFromPoints(param));
        res.setTo(taxiTrajectoryMapper.getToPoints(param));
        return res;
    }

    public List<Point> getOds(ListStatisticsParam param) {
        List<Point> res = new ArrayList<>();
        res.addAll(taxiTrajectoryMapper.getOriginPoints(param));
        res.addAll(taxiTrajectoryMapper.getDestPoints(param));
        return res;
    }
}
