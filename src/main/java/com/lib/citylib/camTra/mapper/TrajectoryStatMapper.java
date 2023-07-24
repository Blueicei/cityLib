package com.lib.citylib.camTra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.query.QueryCamCountByCar;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
* @author fuke
* @description 针对表【trajectory_stat】的数据库操作Mapper
* @createDate 2023-05-24 10:36:50
* @Entity com.lib.citylib.camTra.model.CarTrajectory
*/
public interface TrajectoryStatMapper extends BaseMapper<CamTrajectory> {
    void clear();
    //获取车辆总数、轨迹总数、轨迹点总数
    HashMap<String,Object> getTotalStat(ListStatisticsParam param);
    void insertBatch(@Param("trajectories") List<CarTrajectory> trajectories);

    IPage<TrajectoryStat> listStatByAll(IPage<TrajectoryStat> page, @Param("param")ListStatisticsParam param);
    List<CarTrajectory> getStatByCar(ListStatisticsParam param);
}




