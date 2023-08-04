package com.lib.citylib.camTra.model.taxi;

import com.lib.citylib.camTra.query.Pager;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "查询指定范围的流向")
public class ODResult{
    private List<Point> from;
    private List<Point> to;
}
