package com.lib.citylib.camTra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.model.TrajectoryStat;
import com.lib.citylib.camTra.model.taxi.GpsPoint;
import com.lib.citylib.camTra.model.taxi.TaxiTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
* @author fuke
* @description 针对表【trajectory_stat】的数据库操作Mapper
* @createDate 2023-05-24 10:36:50
* @Entity com.lib.citylib.camTra.model.CarTrajectory
*/
public interface TaxiTrajectoryMapper extends BaseMapper<CamTrajectory> {
    //获取车辆总数、轨迹总数
    HashMap<String,Object> getTotalStat(ListStatisticsParam param);
    void insertBatch(@Param("trajectories") List<TaxiTrajectory> trajectories);
    void insertPoints(@Param("points") List<GpsPoint> points);

    IPage<TrajectoryStat> listStatByAll(IPage<TrajectoryStat> page, @Param("param")ListStatisticsParam param);
    List<TaxiTrajectory> getStatByCar(ListStatisticsParam param);
    List<GpsPoint> getPointByTra(String traId);

    List<GpsPoint> getPointByCar(ListStatisticsParam param);
}




