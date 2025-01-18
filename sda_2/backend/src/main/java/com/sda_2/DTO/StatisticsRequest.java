package com.sda_2.DTO;

import java.util.List;

public class StatisticsRequest {
    private List<Double> data;
    private Integer degree;

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    public Integer getDegree() {
        return degree;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
    }
} 