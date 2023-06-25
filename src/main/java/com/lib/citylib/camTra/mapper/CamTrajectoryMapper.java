package com.lib.citylib.camTra.mapper;
import java.util.Date;
import java.util.List;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.Query.QueryVehicleCountByCam;
import com.lib.citylib.camTra.model.CarNuminCam;
import org.apache.ibatis.annotations.Mapper;
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

    List<CamTrajectory> searchAllByCarNumberOrderInTimeRange(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CamTrajectory> listByTrajectoryDto(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int insertAll(CamTrajectory camTrajectory);

    List<String> vehicleCountByCam(@Param("camId")String camId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<QueryCamCountByCar> listCamCountByCar(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

}




