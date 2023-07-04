package com.lib.citylib.camTra.query;

import lombok.Data;

@Data
public class QueryGenerateResult {
    private String msg;
    private Long carCount;
    private Long traCount;

    public QueryGenerateResult(String msg, Long carCount, Long traCount) {
        this.msg = msg;
        this.carCount = carCount;
        this.traCount = traCount;
    }
}
