package com.lib.citylib;

import com.lib.citylib.camTra.mapper.CamTrajectoryMapper;
import com.lib.citylib.camTra.mapper.TrajectoryStatMapper;
import com.lib.citylib.camTra.model.CarTrajectory;
import com.lib.citylib.camTra.query.ListStatisticsParam;
import com.lib.citylib.camTra.service.TrajectoryStatService;
import com.lib.citylib.camTra.utils.GPSUtil;
import com.lib.citylib.camTra.utils.PartitionTraUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    public void testGen(){
        partitionTraUtil.partitionTraUtil();
    }

    @Test
    public void testCarStat() throws Exception {
        ListStatisticsParam param = new ListStatisticsParam();
        param.setCarNumber("鲁A0001018635");
        trajectoryStatService.getStatByCar(param);
    }

    @Test
    public void saveTaxiTra() throws Exception {
        partitionTraUtil.saveGpsData("");
    }

    @Test void testGps(){
        double lon = 116.98948, lat = 36.673418;
        System.out.println(Arrays.toString(GPSUtil.gps84_To_bd09(lat, lon)));
    }
}
