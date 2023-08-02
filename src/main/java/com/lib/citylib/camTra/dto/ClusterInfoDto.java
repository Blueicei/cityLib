package com.lib.citylib.camTra.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ClusterInfoDto implements Serializable {

    private String clusterName;
    private List<String> points;
    private Boolean asOrigin;
}
