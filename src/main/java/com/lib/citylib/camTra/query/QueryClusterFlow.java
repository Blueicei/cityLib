package com.lib.citylib.camTra.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lib.citylib.camTra.dto.ClusterInfoDto;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class QueryClusterFlow implements Serializable {

    private ClusterInfoDto targetCluster;
    private ClusterInfoDto arrowToCluster;
    private Integer count;
}
