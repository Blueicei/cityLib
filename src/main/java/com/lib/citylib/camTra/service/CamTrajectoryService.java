package com.lib.citylib.camTra.service;

import com.lib.citylib.camTra.dto.TrajectoryDto;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface CamTrajectoryService {
    public List<CamTrajectory> listByCarNumber(String carNumber);
    public CarTrajectory listByCarNumberOrderInTimeRange(String carNumber, Date startTime, Date endtTime);
    public List<CarTrajectory> listByTrajectoryDto(TrajectoryDto trajectoryDto);
    public void insert() throws IOException;
}
