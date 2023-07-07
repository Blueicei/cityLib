package com.lib.citylib.camTra.mapper;
import java.util.Date;
import java.util.List;

import com.lib.citylib.camTra.Query.QueryCamCountByCar;
import com.lib.citylib.camTra.model.*;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author fuke
* @description 针对表【camtrajectory】的数据库操作Mapper
* @createDate 2023-05-24 10:36:50
* @Entity com.lib.citylib.camTra.model.CamTrajectory
*/
public interface CamTrajectoryMapper extends BaseMapper<CamTrajectory> {
    List<CamTrajectory> selectAllByCarNumber(@Param("carNumber") String carNumber);

    List<CamInfo> getAllCamInfo();

    List<CamTrajectory> searchAllByCarNumberOrderInTimeRange(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CamTrajectory> listByTrajectoryDto(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int insertAll(CamTrajectory camTrajectory);

    List<String> vehicleCountByCam(@Param("camId")String camId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<QueryCamCountByCar> listCamCountByCar(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CityFlowStats> cityFlowStats(@Param("camid") String camid, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CamTrajectory> foreignVehiclesStats(@Param("camid") String camid, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<String> getAllCarTypes();

    List<CamTrajectory> compareVehiclesStats(@Param("camid") String camid, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CamTrajectory> multiRegionAnalysis(@Param("left") double left, @Param("right") double right, @Param("up") double up, @Param("down") double down, @Param("startTime") Date startTime,@Param("endTime") Date endTime);

    Date findFirstTime(@Param("carNumber") String carNumber, @Param("startTime") Date startTime,@Param("endTime") Date endTime);


    Point getPoint(@Param("camId") String camId);

    List<CarNumberAndCarTypeByCount> listAllCarNumberAndCarTypeByCount();

//    List<CamInfo> listCamIdAndInfo(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    List<CamInfo> listCamIdAndInfo(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int searchCountByCamIdAndTime(@Param("camId")String camId,@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<CamInfoCount> searchCamInfoCount(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}




