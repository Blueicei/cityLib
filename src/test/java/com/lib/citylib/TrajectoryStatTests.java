package com.lib.citylib;

import com.lib.citylib.camTra.dto.StartToEndTime;
import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.GPSUtil;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
public class TrajectoryStatTests {
    @Resource
    private TrajectoryStatService trajectoryStatService;
    @Resource
    private TrajectoryStatMapper trajectoryStatMapper;
    @Resource
    private CamTrajectoryMapper camTrajectoryMapper;
    @Resource
    private PartitionTraUtil partitionTraUtil;

    @Test
    public void testStat() throws ParseException {
        StartToEndTime startToEndTime = new StartToEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = sdf.parse("2021-2-1 0:00:00");
        Date endTime = sdf.parse("2021-2-1 23:00:00");
        startToEndTime.setStartTime(startTime);
        startToEndTime.setEndTime(endTime);
//        trajectoryStatService.getTableStatByTime1(startToEndTime);
    }

    @Test
    public void testGen(){
        partitionTraUtil.partitionTraUtil();
    }
    @Test
    public void testGenPlus(){
        partitionTraUtil.partitionTraUtilPlus();
    }
    @Test
    public void testCarStat() throws Exception {
        ListStatisticsParam param = new ListStatisticsParam();
        param.setCarNumber("鲁A0001018635");
        trajectoryStatService.getStatByCar(param);
    }

    @Test
    public void saveTaxiTra() throws Exception {
        partitionTraUtil.saveGpsData(null);
    }

    @Test
    public void testGps(){
        double lon = 116.98948, lat = 36.673418;
        System.out.println(Arrays.toString(GPSUtil.gps84_To_bd09(lat, lon)));
    }

    private static final Logger logger = LoggerFactory.getLogger(PartitionTraUtil.class);
    @Test
    public void testLog(){
        logger.error("error");
        logger.warn("warn");
        logger.info("info");
    }

}
