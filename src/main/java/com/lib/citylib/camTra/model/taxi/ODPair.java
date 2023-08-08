package com.lib.citylib.camTra.model.taxi;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "OD对")
public class ODPair {
    private Double startLng;
    private Double startLat;
    private Double EndLng;
    private Double EndLat;
}
