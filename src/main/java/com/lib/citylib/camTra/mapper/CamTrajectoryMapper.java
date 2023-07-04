package com.lib.citylib.camTra.mapper;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lib.citylib.camTra.model.CamInfo;
import com.lib.citylib.camTra.model.CarInfo;
import com.lib.citylib.camTra.model.TableInfo;
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

    Long countCityFlow(@Param("camId")String camId,  @Param("startTime") Date startTime, @Param("endtTime") Date endTime);

    List<CamInfo> getAllCamInfo();

    List<TableInfo> getTableNameList(@Param("tableNameList") List<String> tableNameList);

    List<String> getCarTypeList();
    TableInfo getTableInfo();

    List<CarInfo> getCarNumberList();

}




