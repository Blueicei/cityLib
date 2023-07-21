package com.lib.citylib;

import com.lib.citylib.camTra.model.CamTrajectory;
import com.lib.citylib.camTra.model.CarTrajectory;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class CamTraPreProcess extends KeyedProcessFunction<String, CamTrajectory, CarTrajectory> {

    private ValueState<CarTrajectory> state;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", CarTrajectory.class));
    }

    @Override
    public void processElement(CamTrajectory camTrajectory, KeyedProcessFunction<String, CamTrajectory, CarTrajectory>.Context context, Collector<CarTrajectory> collector) throws Exception {
        //        collector.collect(camTrajectory);
        CarTrajectory current = state.value();
        if (current == null) {
            current = new CarTrajectory(camTrajectory);
        }
        else {current.addPoint(camTrajectory);}
        state.update(current);
        collector.collect(current); // 每次执行processElement都要输出一遍state， ,同样keyBy和reduce也判断不了边界需要持续输出
    }
}
