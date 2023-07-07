package com.lib.citylib.camTra.model;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
//为热力图服务，返回每一个Cam的坐标以及他们统计的次数
public class CamInfoCount {
    private String camId;

    private Double camLon;

    private Double camLat;

    private int count;
}
