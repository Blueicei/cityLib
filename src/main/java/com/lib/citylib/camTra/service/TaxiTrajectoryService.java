package com.lib.citylib.camTra.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lib.citylib.camTra.mapper.TaxiTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<GpsPoint> getPointByTra(String traId) {
        return taxiTrajectoryMapper.getPointByTra(traId);
    }

    public List<GpsPoint> getPointByCar(ListStatisticsParam param) {
        return taxiTrajectoryMapper.getPointByCar(param);
    }
}