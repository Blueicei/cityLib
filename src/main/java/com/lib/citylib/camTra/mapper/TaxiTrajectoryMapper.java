package com.lib.citylib.camTra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.ODPair;
import com.lib.citylib.camTra.model.taxi.Point;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.query.QueryODParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
* @author fuke
* @description 针对表【trajectory_stat】的数据库操作Mapper
* @createDate 2023-05-24 10:36:50
* @Entity com.lib.citylib.camTra.model.CarTrajectory
*/
public interface TaxiTrajectoryMapper extends BaseMapper<CamTrajectory> {
    //获取车辆总数、轨迹总数
    HashMap<String,Object> getTotalStat(ListStatisticsParam param);
    HashMap<String, Object> getTableStat(ListStatisticsParam param);
    void insertBatch(@Param("trajectories") List<TaxiTrajectory> trajectories);
    void insertPoints(@Param("points") List<GpsPoint> points);

    IPage<TrajectoryStat> listStatByAll(IPage<TrajectoryStat> page, @Param("param")ListStatisticsParam param);
    List<TaxiTrajectory> getStatByCar(ListStatisticsParam param);

    //获取gps点
    List<GpsPoint> getGpsPointByTra(String traId);
    List<GpsPoint> getGpsPointByCar(ListStatisticsParam param);
    List<GpsPoint> getGpsPoints(ListStatisticsParam param);

    //获取卡口点
    List<CamTrajectory> getCamPointByCar(ListStatisticsParam param);

    Set<String> getCarFromStat();
    Set<String> getCarFromPoint();

    //起点集合、终点集合
    List<Point> getOriginPoints(ListStatisticsParam param);
    List<Point> getDestPoints(ListStatisticsParam param);
    List<ODPair> getODPairs(ListStatisticsParam param);
    // 流入该区域的起点
    List<Point> getFromPoints(QueryODParam param);
    List<Point> getToPoints(QueryODParam param);

    List<TableStatDateTraCount> getTableStatDateTraCount(ListStatisticsParam param);
    List<TableStatDistanceTraCount> getTableStatDistanceTraCount(ListStatisticsParam param);
    List<TableStatTimeIntervalTraCount> getTableStatTimeIntervalTraCount(ListStatisticsParam param);
    List<TableStatPerDayHourCount> getTableStatPerDayHourCount(ListStatisticsParam param);
    List<TableStatPerDayTraCount> getTableStatPerDayTraCount(ListStatisticsParam param);
    List<TableStatTraCount> getTableStatTraCountByCar(ListStatisticsParam param);
}




