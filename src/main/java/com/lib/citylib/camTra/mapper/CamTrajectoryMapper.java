package com.lib.citylib.camTra.mapper;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.lib.citylib.camTra.model.CamTrajectory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author fuke
* @description 针对表【camtrajectory】的数据库操作Mapper
* @createDate 2023-05-24 10:36:50
* @Entity com.lib.citylib.camTra.model.CamTrajectory
*/
public interface CamTrajectoryMapper extends BaseMapper<CamTrajectory> {
    List<CamTrajectory> selectAllByCarNumber(@Param("carNumber") String carNumber);

    List<CamTrajectory> searchAllByCarNumberOrderInTimeRange(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endtTime") Date endTime);
    int insertAll(CamTrajectory camTrajectory);

}




