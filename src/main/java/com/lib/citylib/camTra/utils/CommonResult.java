package com.lib.citylib.camTra.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;

@Data

public class CommonResult extends HashMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 64161316464L;

    public CommonResult() {
        this.put("code", 0);
    }

    public static CommonResult ok() {
        CommonResult result = new CommonResult();
        result.put("code", 200);
        return result;
    }

    public static CommonResult success(Object msg) {
        CommonResult result = new CommonResult();
        result.put("code", 200);
        result.put("msg", msg);
        return result;
    }

    public static CommonResult error() {
        return error(500, "系统异常");
    }

    public static CommonResult error(String msg) {
        return error(500, msg);
    }

    public static CommonResult error(int code, String msg) {
        CommonResult result = new CommonResult();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    public CommonResult put(Object data) {
        super.put("data", data);
        return this;
    }
}
