package com.lib.citylib.camTra.service.impl;


import com.lib.citylib.camTra.dto.TrajectoryDto;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.service.CamTrajectoryService;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class CamTrajectoryServiceImpl implements CamTrajectoryService {
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;

    public List<CamTrajectory> listByCarNumber(String carNumber) {
        return camTrajectoryMapper.selectAllByCarNumber(carNumber);
    }
    public CarTrajectory listByCarNumberOrderInTimeRange(String carNumber, Date startTime, Date endtTime) {
        List<CamTrajectory> camTraList = camTrajectoryMapper.searchAllByCarNumberOrderInTimeRange(carNumber, startTime, endtTime);
        if (camTraList.size()>0) {
            String carType = camTraList.get(0).getCarType();
            return new CarTrajectory(carNumber, carType, camTraList);
        }
        return new CarTrajectory(carNumber, new ArrayList<CamTrajectory>());
    }

    public List<CarTrajectory> listByTrajectoryDto(TrajectoryDto trajectoryDto) {
        return null;
    }

    public void insert() throws IOException {
        File dir = new File("D:\\workspace_py\\TrajMatchV1\\data\\202102");
        File[] files = dir.listFiles();
        for(File fileName : files){
            try (FileInputStream fis = new FileInputStream(fileName);
                 InputStreamReader isr = new InputStreamReader(fis,
                         StandardCharsets.UTF_8);
                 CSVReader reader = new CSVReader(isr)) {
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    String carNumber = nextLine[0].split("-")[0];
                    String carType = nextLine[0].split("-")[1];
                    String[] pointList = Arrays.copyOfRange(nextLine,1, nextLine.length);
//                    System.out.format("%s\n", carNumber);
                    for (String point : pointList) {
                        String camId = point.split("-")[0];
                        String direction = point.split("-")[1].split("@")[0];
                        String photoTime = point.split("@")[1];
//                        System.out.format("%s %s %s\n", camId, direction, photoTime);
                        CamTrajectory camTra = new CamTrajectory();
                        camTra.setCarNumber(carNumber);
                        camTra.setCarType(carType);
                        camTra.setCamId(camId);
                        camTra.setDirection(direction);
                        camTra.setPhotoTime(new Date(Long.parseLong(photoTime)));
                        camTrajectoryMapper.insertAll(camTra);
                    }
                }
                System.exit(0);
            }
        }
    }
}
