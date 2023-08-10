package com.lib.citylib.camTra.mapper;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lib.citylib.camTra.dto.StartToEndTime;
import com.lib.citylib.camTra.model.*;
import com.lib.citylib.camTra.query.QueryCamCount;
import com.lib.citylib.camTra.query.QueryCamCountByCar;
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

    List<CamTrajectory> searchAllByCarNumberOrderInTimeRange(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endtTime") Date endTime);

    int insertAll(CamTrajectory camTrajectory);

    Long countCityFlow(@Param("camId")String camId,  @Param("startTime") Date startTime, @Param("endtTime") Date endTime);

    List<CamInfo> getAllCamInfo();
    CamInfo getCamInfo(@Param("camId")String camId);

    List<TableInfo> getTableNameList(@Param("tableNameList") List<String> tableNameList);
    List<String> getCamTraTableNameList();

    List<String> getCarTypeList();
    TableInfo getTableInfo();

    List<CarInfo> getCarNumberList();
    List<CarInfo> getCarNumberListInCondition(@Param("carNumber") List<String> carNumber, @Param("carType") List<String> carType);
    List<CamTrajectory> getPartialCarPointInCondition(@Param("carInfoList") List<CarInfo> carInfoList, @Param("filterTraRange") Boolean filterTraRange);
    List<CamTrajectory> getAllPointInCondition(@Param("carNumber") List<String> carNumber, @Param("carType") List<String> carType, @Param("filterTraRange") Boolean filterTraRange);

    List<QueryCamCountByCar> listCamCountByCar(@Param("carNumber") String carNumber, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
    Long getClusterFlow(@Param("carNumber") String carNumber, @Param("points") List<String> points);

    List<QueryCamCount> listCamCount(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    List<QueryCamCount> listCamCountInRange(@Param("startToEndTimeList") List<StartToEndTime> startToEndTimeList);

}




