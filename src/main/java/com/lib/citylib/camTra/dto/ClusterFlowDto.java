package com.lib.citylib.camTra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ClusterFlowDto implements Serializable {

    private List<ClusterInfoDto> allCluster;
    private List<ClusterInfoDto> targetCluster;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date searchDate;
    private Integer cutTime;
}
