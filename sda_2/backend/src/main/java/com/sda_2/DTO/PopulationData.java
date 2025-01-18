package com.sda_2.DTO;

import java.util.HashMap;
import java.util.Map;

public class PopulationData {
    private String geography;
    private Map<String, Integer> quarterData;

    public PopulationData() {
        this.quarterData = new HashMap<>();
    }

    public String getGeography() {
        return geography;
    }

    public void setGeography(String geography) {
        this.geography = geography;
    }

    public Map<String, Integer> getQuarterData() {
        return quarterData;
    }

    public void setQuarterData(Map<String, Integer> quarterData) {
        this.quarterData = quarterData != null ? quarterData : new HashMap<>();
    }

    // 动态获取季度数据的方法
    public Integer getQuarterValue(String quarter) {
        return quarterData.get(quarter);
    }

    // 动态设置季度数据的方法
    public void setQuarterValue(String quarter, Integer value) {
        if (quarterData == null) {
            quarterData = new HashMap<>();
        }
        quarterData.put(quarter, value);
    }
}
