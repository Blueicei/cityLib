package com.lib.citylib.camTra.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class CamLocation {
    HashMap<String ,Point> map;
    public CamLocation() {
        map = new HashMap<>();
    }

    public void addToHashMap(String key, Point value) {
         map.put(key, value);
    }

    public Point getValueFromHashMap(String key) {
        return map.get(key);
    }
}
