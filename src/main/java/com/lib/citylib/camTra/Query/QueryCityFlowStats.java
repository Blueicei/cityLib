package com.lib.citylib.camTra.Query;

import com.lib.citylib.camTra.model.CityFlowStats;
import lombok.Data;

import java.util.List;

@Data
public class QueryCityFlowStats {
    private String camId;

    private int totalFlow;

    private List<CityFlowStats> cityFlowStats;
}
